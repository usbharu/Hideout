package dev.usbharu.hideout.mastodon.infrastructure.exposedquery

import dev.usbharu.hideout.core.infrastructure.exposedrepository.*
import dev.usbharu.hideout.core.service.media.FileType
import dev.usbharu.hideout.domain.mastodon.model.generated.Account
import dev.usbharu.hideout.domain.mastodon.model.generated.MediaAttachment
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.mastodon.interfaces.api.status.StatusQuery
import dev.usbharu.hideout.mastodon.query.StatusQueryService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class StatusQueryServiceImpl : StatusQueryService {
    @Suppress("LongMethod")
    override suspend fun findByPostIds(ids: List<Long>): List<Status> = findByPostIdsWithMediaAttachments(ids)

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
                it[Media.id] to it.toMedia().let {
                    MediaAttachment(
                        id = it.id.toString(),
                        type = when (it.type) {
                            FileType.Image -> MediaAttachment.Type.image
                            FileType.Video -> MediaAttachment.Type.video
                            FileType.Audio -> MediaAttachment.Type.audio
                            FileType.Unknown -> MediaAttachment.Type.unknown
                        },
                        url = it.url,
                        previewUrl = it.thumbnailUrl,
                        remoteUrl = it.remoteUrl,
                        description = "",
                        blurhash = it.blurHash,
                        textUrl = it.url
                    )
                }
            }

        return statusQueries.mapNotNull {
            postMap[it.postId]?.copy(
                inReplyToId = it.replyId?.toString(),
                inReplyToAccountId = postMap[it.replyId]?.account?.id,
                reblog = postMap[it.repostId],
                mediaAttachments = it.mediaIds.mapNotNull { mediaMap[it] }
            )
        }
    }

    @Suppress("unused")
    private suspend fun internalFindByPostIds(ids: List<Long>): List<Status> {
        val pairs = Posts
            .innerJoin(Users, onColumn = { Posts.userId }, otherColumn = { Users.id })
            .select { Posts.id inList ids }
            .map {
                toStatus(it) to it[Posts.repostId]
            }

        return resolveReplyAndRepost(pairs)
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

    @Suppress("FunctionMaxLength")
    private suspend fun findByPostIdsWithMediaAttachments(ids: List<Long>): List<Status> {
        val pairs = Posts
            .leftJoin(PostsMedia)
            .leftJoin(Users)
            .leftJoin(Media)
            .select { Posts.id inList ids }
            .groupBy { it[Posts.id] }
            .map { it.value }
            .map {
                toStatus(it.first()).copy(
                    mediaAttachments = it.mapNotNull {
                        it.toMediaOrNull()?.let {
                            MediaAttachment(
                                id = it.id.toString(),
                                type = when (it.type) {
                                    FileType.Image -> MediaAttachment.Type.image
                                    FileType.Video -> MediaAttachment.Type.video
                                    FileType.Audio -> MediaAttachment.Type.audio
                                    FileType.Unknown -> MediaAttachment.Type.unknown
                                },
                                url = it.url,
                                previewUrl = it.thumbnailUrl,
                                remoteUrl = it.remoteUrl,
                                description = "",
                                blurhash = it.blurHash,
                                textUrl = it.url
                            )
                        }
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
