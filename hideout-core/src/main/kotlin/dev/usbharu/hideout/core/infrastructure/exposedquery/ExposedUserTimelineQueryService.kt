package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.application.post.ActorDetail
import dev.usbharu.hideout.core.application.post.MediaDetail
import dev.usbharu.hideout.core.application.post.PostDetail
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.infrastructure.exposedrepository.*
import dev.usbharu.hideout.core.query.usertimeline.UserTimelineQueryService
import org.jetbrains.exposed.sql.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.net.URI

@Repository
class ExposedUserTimelineQueryService : UserTimelineQueryService, AbstractRepository() {

    override val logger: Logger
        get() = Companion.logger

    protected fun authorizedQuery(principal: Principal? = null): QueryAlias {
        if (principal == null) {
            return Posts
                .selectAll()
                .where {
                    Posts.visibility eq Visibility.PUBLIC.name or (Posts.visibility eq Visibility.UNLISTED.name)
                }.alias("authorized_table")
        }

        val relationshipsAlias = Relationships.alias("inverse_relationships")

        return Posts
            .leftJoin(PostsVisibleActors)
            .leftJoin(Relationships, onColumn = { Posts.actorId }, otherColumn = { actorId })
            .leftJoin(
                relationshipsAlias,
                onColumn = { Posts.actorId },
                otherColumn = { relationshipsAlias[Relationships.targetActorId] }
            )
            .select(Posts.columns)
            .where {
                Posts.visibility eq Visibility.PUBLIC.name or
                    (Posts.visibility eq Visibility.UNLISTED.name) or
                    (Posts.visibility eq Visibility.DIRECT.name and (PostsVisibleActors.actorId eq principal.actorId.id)) or
                    (Posts.visibility eq Visibility.FOLLOWERS.name and (Relationships.blocking eq false and (relationshipsAlias[Relationships.following] eq true))) or
                    (Posts.actorId eq principal.actorId.id)
            }
            .alias("authorized_table")
    }

    override suspend fun findByIdAll(idList: List<PostId>, principal: Principal): List<PostDetail> {
        val authorizedQuery = authorizedQuery(principal)

        val iconMedia = Media.alias("ICON_MEDIA")

        return authorizedQuery
            .leftJoin(PostsVisibleActors, { authorizedQuery[Posts.id] }, { PostsVisibleActors.postId })
            .leftJoin(Actors, { authorizedQuery[Posts.actorId] }, { Actors.id })
            .leftJoin(iconMedia, { Actors.icon }, { iconMedia[Media.id] })
            .leftJoin(PostsMedia, { authorizedQuery[Posts.id] }, { PostsMedia.postId })
            .leftJoin(Media, { PostsMedia.mediaId }, { Media.id })
            .selectAll()
            .where { authorizedQuery[Posts.id] inList idList.map { it.id } }
            .groupBy { it[authorizedQuery[Posts.id]] }
            .map { it.value }
            .map {
                toPostDetail(it.first(), authorizedQuery, iconMedia).copy(
                    mediaDetailList = it.mapNotNull { resultRow ->
                        resultRow.toMediaOrNull()?.let { it1 -> MediaDetail.of(it1) }
                    }
                )
            }
    }

    private fun toPostDetail(it: ResultRow, authorizedQuery: QueryAlias, iconMedia: Alias<Media>): PostDetail {
        return PostDetail(
            id = it[authorizedQuery[Posts.id]],
            actor = ActorDetail(
                actorId = it[authorizedQuery[Posts.actorId]],
                instanceId = it[Actors.instance],
                name = it[Actors.name],
                domain = it[Actors.domain],
                screenName = it[Actors.screenName],
                url = URI.create(it[Actors.url]),
                locked = it[Actors.locked],
                icon = it.getOrNull(iconMedia[Media.url])?.let { URI.create(it) }
            ),
            overview = it[authorizedQuery[Posts.overview]],
            text = it[authorizedQuery[Posts.text]],
            content = it[authorizedQuery[Posts.content]],
            createdAt = it[authorizedQuery[Posts.createdAt]],
            visibility = Visibility.valueOf(it[authorizedQuery[Posts.visibility]]),
            pureRepost = false,
            url = URI.create(it[authorizedQuery[Posts.url]]),
            apId = URI.create(it[authorizedQuery[Posts.apId]]),
            repost = null,
            reply = null,
            sensitive = it[authorizedQuery[Posts.sensitive]],
            deleted = it[authorizedQuery[Posts.deleted]],
            mediaDetailList = emptyList(),
            moveTo = null,
            emptyList()
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedUserTimelineQueryService::class.java)
    }
}
