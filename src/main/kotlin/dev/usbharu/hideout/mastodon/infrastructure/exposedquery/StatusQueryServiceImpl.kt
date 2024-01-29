package dev.usbharu.hideout.mastodon.infrastructure.exposedquery

import dev.usbharu.hideout.application.infrastructure.exposed.Page
import dev.usbharu.hideout.application.infrastructure.exposed.PaginationList
import dev.usbharu.hideout.application.infrastructure.exposed.pagination
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmoji
import dev.usbharu.hideout.core.domain.model.media.toMediaAttachments
import dev.usbharu.hideout.core.infrastructure.exposedrepository.*
import dev.usbharu.hideout.domain.mastodon.model.generated.Account
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.domain.mastodon.model.generated.Status.Visibility.*
import dev.usbharu.hideout.mastodon.interfaces.api.status.StatusQuery
import dev.usbharu.hideout.mastodon.query.StatusQueryService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import java.time.Instant
import dev.usbharu.hideout.domain.mastodon.model.generated.CustomEmoji as MastodonEmoji

@Suppress("IncompleteDestructuring")
@Repository
class StatusQueryServiceImpl : StatusQueryService {
    override suspend fun findByPostIds(ids: List<Long>): List<Status> = findByPostIdsWithMedia(ids)

    override suspend fun findByPostIdsWithMediaIds(statusQueries: List<StatusQuery>): List<Status> {
        val postIdSet = mutableSetOf<Long>()
        postIdSet.addAll(statusQueries.flatMap { listOfNotNull(it.postId, it.replyId, it.repostId) })
        val mediaIdSet = mutableSetOf<Long>()
        mediaIdSet.addAll(statusQueries.flatMap { it.mediaIds })

        val emojiIdSet = mutableSetOf<Long>()
        emojiIdSet.addAll(statusQueries.flatMap { it.emojiIds })

        val postMap = Posts
            .leftJoin(Actors)
            .select { Posts.id inList postIdSet }
            .associate { it[Posts.id] to toStatus(it) }
        val mediaMap = Media.select { Media.id inList mediaIdSet }
            .associate {
                it[Media.id] to it.toMedia().toMediaAttachments()
            }

        val emojiMap = CustomEmojis.select { CustomEmojis.id inList emojiIdSet }.associate {
            it[CustomEmojis.id] to it.toCustomEmoji().toMastodonEmoji()
        }
        return statusQueries.mapNotNull { statusQuery ->
            postMap[statusQuery.postId]?.copy(
                inReplyToId = statusQuery.replyId?.toString(),
                inReplyToAccountId = postMap[statusQuery.replyId]?.account?.id,
                reblog = postMap[statusQuery.repostId],
                mediaAttachments = statusQuery.mediaIds.mapNotNull { mediaMap[it] },
                emojis = statusQuery.emojiIds.mapNotNull { emojiMap[it] }
            )
        }
    }

    override suspend fun accountsStatus(
        accountId: Long,
        maxId: Long?,
        sinceId: Long?,
        minId: Long?,
        limit: Int,
        onlyMedia: Boolean,
        excludeReplies: Boolean,
        excludeReblogs: Boolean,
        pinned: Boolean,
        tagged: String?,
        includeFollowers: Boolean
    ): List<Status> {
        val query = Posts
            .leftJoin(PostsMedia)
            .leftJoin(Actors)
            .leftJoin(Media)
            .select { Posts.actorId eq accountId }.limit(20)

        if (maxId != null) {
            query.andWhere { Posts.id eq maxId }
        }
        if (sinceId != null) {
            query.andWhere { Posts.id eq sinceId }
        }
        if (minId != null) {
            query.andWhere { Posts.id eq minId }
        }
        if (onlyMedia) {
            query.andWhere { PostsMedia.mediaId.isNotNull() }
        }
        if (excludeReplies) {
            query.andWhere { Posts.replyId.isNotNull() }
        }
        if (excludeReblogs) {
            query.andWhere { Posts.repostId.isNotNull() }
        }
        if (includeFollowers) {
            query.andWhere { Posts.visibility inList listOf(public.ordinal, unlisted.ordinal, private.ordinal) }
        } else {
            query.andWhere { Posts.visibility inList listOf(public.ordinal, unlisted.ordinal) }
        }

        val pairs = query.groupBy { it[Posts.id] }
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

    override suspend fun accountsStatus(
        accountId: Long,
        onlyMedia: Boolean,
        excludeReplies: Boolean,
        excludeReblogs: Boolean,
        pinned: Boolean,
        tagged: String?,
        includeFollowers: Boolean,
        page: Page
    ): PaginationList<Status, Long> {
        val query = Posts
            .leftJoin(PostsMedia)
            .leftJoin(Actors)
            .leftJoin(Media)
            .select { Posts.actorId eq accountId }

        query.pagination(page, Posts.id)

        if (onlyMedia) {
            query.andWhere { PostsMedia.mediaId.isNotNull() }
        }
        if (excludeReplies) {
            query.andWhere { Posts.replyId.isNotNull() }
        }
        if (excludeReblogs) {
            query.andWhere { Posts.repostId.isNotNull() }
        }
        if (includeFollowers) {
            query.andWhere { Posts.visibility inList listOf(public.ordinal, unlisted.ordinal, private.ordinal) }
        } else {
            query.andWhere { Posts.visibility inList listOf(public.ordinal, unlisted.ordinal) }
        }

        val pairs = query.groupBy { it[Posts.id] }
            .map { it.value }
            .map {
                toStatus(it.first()).copy(
                    mediaAttachments = it.mapNotNull { resultRow ->
                        resultRow.toMediaOrNull()?.toMediaAttachments()
                    }
                ) to it.first()[Posts.repostId]
            }

        val statuses = resolveReplyAndRepost(pairs)
        return PaginationList(
            statuses,
            statuses.lastOrNull()?.id?.toLongOrNull(),
            statuses.firstOrNull()?.id?.toLongOrNull()
        )
    }

    override suspend fun findByPostId(id: Long): Status {
        val map = Posts
            .leftJoin(PostsMedia)
            .leftJoin(Actors)
            .leftJoin(Media)
            .select { Posts.id eq id }
            .groupBy { it[Posts.id] }
            .map { it.value }
            .map {
                toStatus(it.first()).copy(
                    mediaAttachments = it.mapNotNull { resultRow ->
                        resultRow.toMediaOrNull()?.toMediaAttachments()
                    },
                    emojis = it.mapNotNull { resultRow -> resultRow.toCustomEmojiOrNull()?.toMastodonEmoji() }
                ) to it.first()[Posts.repostId]
            }
        return resolveReplyAndRepost(map).single()
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
            .leftJoin(PostsEmojis)
            .leftJoin(CustomEmojis)
            .leftJoin(Actors)
            .leftJoin(Media)
            .select { Posts.id inList ids }
            .groupBy { it[Posts.id] }
            .map { it.value }
            .map {
                toStatus(it.first()).copy(
                    mediaAttachments = it.mapNotNull { resultRow ->
                        resultRow.toMediaOrNull()?.toMediaAttachments()
                    },
                    emojis = it.mapNotNull { resultRow -> resultRow.toCustomEmojiOrNull()?.toMastodonEmoji() }
                ) to it.first()[Posts.repostId]
            }
        return resolveReplyAndRepost(pairs)
    }
}

private fun CustomEmoji.toMastodonEmoji(): MastodonEmoji = MastodonEmoji(
    shortcode = this.name,
    url = this.url,
    staticUrl = this.url,
    visibleInPicker = true,
    category = this.category.orEmpty()
)

private fun toStatus(it: ResultRow) = Status(
    id = it[Posts.id].toString(),
    uri = it[Posts.apId],
    createdAt = Instant.ofEpochMilli(it[Posts.createdAt]).toString(),
    account = Account(
        id = it[Actors.id].toString(),
        username = it[Actors.name],
        acct = "${it[Actors.name]}@${it[Actors.domain]}",
        url = it[Actors.url],
        displayName = it[Actors.screenName],
        note = it[Actors.description],
        avatar = it[Actors.url] + "/icon.jpg",
        avatarStatic = it[Actors.url] + "/icon.jpg",
        header = it[Actors.url] + "/header.jpg",
        headerStatic = it[Actors.url] + "/header.jpg",
        locked = it[Actors.locked],
        fields = emptyList(),
        emojis = emptyList(),
        bot = false,
        group = false,
        discoverable = true,
        createdAt = Instant.ofEpochMilli(it[Actors.createdAt]).toString(),
        lastStatusAt = it[Actors.lastPostAt]?.toString(),
        statusesCount = it[Actors.postsCount],
        followersCount = it[Actors.followersCount],
        followingCount = it[Actors.followingCount],
        noindex = false,
        moved = false,
        suspendex = false,
        limited = false
    ),
    content = it[Posts.text],
    visibility = when (it[Posts.visibility]) {
        0 -> public
        1 -> unlisted
        2 -> private
        3 -> direct
        else -> public
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
