package utils

import dev.usbharu.hideout.config.ApplicationConfig
import dev.usbharu.hideout.config.CharacterLimit
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.service.core.TwitterSnowflakeIdGenerateService
import kotlinx.coroutines.runBlocking
import java.net.URL
import java.time.Instant

object UserBuilder {
    private val userBuilder = User.UserBuilder(CharacterLimit(), ApplicationConfig(URL("https://example.com")))

    private val idGenerator = TwitterSnowflakeIdGenerateService

    suspend fun localUserOf(
        id: Long = generateId(),
        name: String = "test-user-$id",
        domain: String = "example.com",
        screenName: String = name,
        description: String = "This user is test user.",
        password: String = "password-$id",
        inbox: String = "https://$domain/$id/inbox",
        outbox: String = "https://$domain/$id/outbox",
        url: String = "https://$domain/$id/",
        publicKey: String = "-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----",
        privateKey: String = "-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----",
        createdAt: Instant = Instant.now(),
        keyId: String = "https://$domain/$id#pubkey",
        followers: String = "https://$domain/$id/followers",
        following: String = "https://$domain/$id/following"
    ): User {
        return userBuilder.of(
            id = id,
            name = name,
            domain = domain,
            screenName = screenName,
            description = description,
            password = password,
            inbox = inbox,
            outbox = outbox,
            url = url,
            publicKey = publicKey,
            privateKey = privateKey,
            createdAt = createdAt,
            keyId = keyId,
            followers = following,
            following = followers
        )
    }

    private fun generateId(): Long = runBlocking {
        idGenerator.generateId()
    }
}
