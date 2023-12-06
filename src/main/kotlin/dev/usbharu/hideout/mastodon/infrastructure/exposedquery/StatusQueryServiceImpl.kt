package dev.usbharu.hideout.mastodon.infrastructure.exposedquery

import dev.usbharu.hideout.core.domain.model.media.toMediaAttachments
import dev.usbharu.hideout.core.infrastructure.exposedrepository.*
import dev.usbharu.hideout.domain.mastodon.model.generated.Account
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.mastodon.interfaces.api.status.StatusQuery
import dev.usbharu.hideout.mastodon.query.StatusQueryService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import java.time.Instant

@Suppress("IncompleteDestructuring")
@Repository
class StatusQueryServiceImpl : StatusQueryService {
    override suspend fun findByPostIds(ids: List<Long>): List<Status> = findByPostIdsWithMedia(ids)

    override suspend fun findByPostIdsWithMediaIds(statusQueries: List<StatusQuery>): List<Status> {
        val postIdSet = mutableSetOf<Long>()
        postIdSet.addAll(statusQueries.flatMap { listOfNotNull(it.postId, it.replyId, it.repostId) })
        val mediaIdSet = mutableSetOf<Long>()
        mediaIdSet.addAll(statusQueries.flatMap { it.mediaIds })
        val postMap = Posts
            .leftJoin(Users)
            .select { Posts.id inList postIdSet }
            .associate { it[Posts.id] to toStatus(it) }
        val mediaMap = Media.select { Media.id inList mediaIdSet }
            .associate {
                it[Media.id] to it.toMedia().toMediaAttachments()
            }

        return statusQueries.mapNotNull { statusQuery ->
            postMap[statusQuery.postId]?.copy(
                inReplyToId = statusQuery.replyId?.toString(),
                inReplyToAccountId = postMap[statusQuery.replyId]?.account?.id,
                reblog = postMap[statusQuery.repostId],
                mediaAttachments = statusQuery.mediaIds.mapNotNull { mediaMap[it] }
            )
        }
    }

    private fun resolveReplyAndRepost(pairs: List<Pair<Status, Long?>>): List<Status> {
        val statuses = pairs.map { it.first }
        return pairs
            .map {
                if (it.second != null) {
                    it.first.copy(reblog = statuses.find { (id) -> id == it.second.toString() })
                } else {
                    it.first
                }
            }
            .map {
                if (it.inReplyToId != null) {
                    it.copy(inReplyToAccountId = statuses.find { (id) -> id == it.inReplyToId }?.id)
                } else {
                    it
                }
            }
    }

    private suspend fun findByPostIdsWithMedia(ids: List<Long>): List<Status> {
        val pairs = Posts
            .leftJoin(PostsMedia)
            .leftJoin(Users)
            .leftJoin(Media)
            .select { Posts.id inList ids }
            .groupBy { it[Posts.id] }
            .map { it.value }
            .map {
                toStatus(it.first()).copy(
                    mediaAttachments = it.mapNotNull { resultRow ->
                        resultRow.toMediaOrNull()?.toMediaAttachments()
                    }
                ) to it.first()[Posts.repostId]
            }
        return resolveReplyAndRepost(pairs)
    }
}

private fun toStatus(it: ResultRow) = Status(
    id = it[Posts.id].toString(),
    uri = it[Posts.apId],
    createdAt = Instant.ofEpochMilli(it[Posts.createdAt]).toString(),
    account = Account(
        id = it[Users.id].toString(),
        username = it[Users.name],
        acct = "${it[Users.name]}@${it[Users.domain]}",
        url = it[Users.url],
        displayName = it[Users.screenName],
        note = it[Users.description],
        avatar = it[Users.url] + "/icon.jpg",
        avatarStatic = it[Users.url] + "/icon.jpg",
        header = it[Users.url] + "/header.jpg",
        headerStatic = it[Users.url] + "/header.jpg",
        locked = false,
        fields = emptyList(),
        emojis = emptyList(),
        bot = false,
        group = false,
        discoverable = true,
        createdAt = Instant.ofEpochMilli(it[Users.createdAt]).toString(),
        lastStatusAt = Instant.ofEpochMilli(it[Users.createdAt]).toString(),
        statusesCount = 0,
        followersCount = 0,
        followingCount = 0,
        noindex = false,
        moved = false,
        suspendex = false,
        limited = false
    ),
    content = it[Posts.text],
    visibility = when (it[Posts.visibility]) {
        0 -> Status.Visibility.public
        1 -> Status.Visibility.unlisted
        2 -> Status.Visibility.private
        3 -> Status.Visibility.direct
        else -> Status.Visibility.public
    },
    sensitive = it[Posts.sensitive],
    spoilerText = it[Posts.overview].orEmpty(),
    mediaAttachments = emptyList(),
    mentions = emptyList(),
    tags = emptyList(),
    emojis = emptyList(),
    reblogsCount = 0,
    favouritesCount = 0,
    repliesCount = 0,
    url = it[Posts.apId],
    inReplyToId = it[Posts.replyId]?.toString(),
    inReplyToAccountId = null,
    language = null,
    text = it[Posts.text],
    editedAt = null
)
