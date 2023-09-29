package dev.usbharu.hideout.query.mastodon

import dev.usbharu.hideout.domain.mastodon.model.generated.Account
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.repository.Posts
import dev.usbharu.hideout.repository.Users
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class StatusQueryServiceImpl : StatusQueryService {
    override suspend fun findByPostIds(ids: List<Long>): List<Status> {

        val pairs = Posts.innerJoin(Users, onColumn = { userId }, otherColumn = { id })
            .select { Posts.id inList ids }
            .map {
                Status(
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
                    inReplyToId = it[Posts.replyId].toString(),
                    inReplyToAccountId = null,
                    language = null,
                    text = it[Posts.text],
                    editedAt = null,
                    application = null,
                    poll = null,
                    card = null,
                    favourited = null,
                    reblogged = null,
                    muted = null,
                    bookmarked = null,
                    pinned = null,
                    filtered = null
                ) to it[Posts.repostId]
            }

        val statuses = pairs.map { it.first }
        return pairs
            .map {
                if (it.second != null) {
                    it.first.copy(reblog = statuses.find { status -> status.id == it.second.toString() })
                } else {
                    it.first
                }
            }
            .map {
                if (it.inReplyToId != null) {
                    it.copy(inReplyToAccountId = statuses.find { status -> status.id == it.inReplyToId }?.id)
                } else {
                    it
                }
            }


    }
}
