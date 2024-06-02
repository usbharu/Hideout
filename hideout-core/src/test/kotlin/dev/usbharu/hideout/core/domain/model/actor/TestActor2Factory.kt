package dev.usbharu.hideout.core.domain.model.actor

import dev.usbharu.hideout.application.service.id.TwitterSnowflakeIdGenerateService
import dev.usbharu.hideout.core.domain.model.emoji.EmojiId
import dev.usbharu.hideout.core.domain.model.instance.InstanceId
import dev.usbharu.hideout.core.domain.model.shared.Domain
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.time.Instant

object TestActor2Factory {
    private val idGenerateService = TwitterSnowflakeIdGenerateService

    fun create(
        id: Long = generateId(),
        actorName: String = "test-$id",
        domain: String = "example.com",
        actorScreenName: String = actorName,
        description: String = "test description",
        inbox: URI = URI.create("https://example.com/$id/inbox"),
        outbox: URI = URI.create("https://example.com/$id/outbox"),
        uri: URI = URI.create("https://example.com/$id"),
        publicKey: ActorPublicKey,
        privateKey: ActorPrivateKey? = null,
        createdAt: Instant = Instant.now(),
        keyId: String = "https://example.com/$id#key-id",
        followersEndpoint: URI = URI.create("https://example.com/$id/followers"),
        followingEndpoint: URI = URI.create("https://example.com/$id/following"),
        instanceId: Long = 1L,
        locked: Boolean = false,
        followersCount: Int = 0,
        followingCount: Int = 0,
        postCount: Int = 0,
        lastPostDate: Instant? = null,
        suspend: Boolean = false,
        alsoKnownAs: Set<ActorId> = emptySet(),
        moveTo: ActorId? = null,
        emojiIds: Set<EmojiId> = emptySet(),
        deleted: Boolean = false,
    ): Actor {
        return runBlocking {
            Actor(
                id = ActorId(id),
                name = ActorName(actorName),
                domain = Domain(domain),
                screenName = ActorScreenName(actorScreenName),
                description = ActorDescription(description),
                inbox = inbox,
                outbox = outbox,
                url = uri,
                publicKey = publicKey,
                privateKey = privateKey,
                createdAt = createdAt,
                keyId = ActorKeyId(keyId),
                followersEndpoint = followersEndpoint,
                followingEndpoint = followingEndpoint,
                instance = InstanceId(instanceId),
                locked = locked,
                followersCount = ActorRelationshipCount(followersCount),
                followingCount = ActorRelationshipCount(followingCount),
                postsCount = ActorPostsCount(postCount),
                lastPostAt = lastPostDate,
                suspend = suspend,
                alsoKnownAs = alsoKnownAs,
                moveTo = moveTo,
                emojiIds = emojiIds,
                deleted = deleted,
            )
        }
    }

    private fun generateId(): Long = runBlocking {
        idGenerateService.generateId()
    }
}