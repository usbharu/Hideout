package dev.usbharu.hideout.service

import dev.usbharu.hideout.ap.*
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.plugins.postAp
import io.ktor.client.*

class ActivityPubUserService(
    private val httpClient: HttpClient,
    private val userService: UserService,
    private val userAuthService: IUserAuthService,
    private val webFingerService: IWebFingerService
) {
    suspend fun generateUserModel(name: String): Person {
        val userEntity = userService.findByName(name)
        val userAuthEntity = userAuthService.findByUserId(userEntity.id)
        val userUrl = "${Config.configData.url}/users/$name"
        return Person(
            type = emptyList(),
            name = userEntity.name,
            id = userUrl,
            preferredUsername = name,
            summary = userEntity.description,
            inbox = "$userUrl/inbox",
            outbox = "$userUrl/outbox",
            url = userUrl,
            icon = Image(
                type = emptyList(),
                name = "$userUrl/icon.png",
                mediaType = "image/png",
                url = "$userUrl/icon.png"
            ),
            publicKey = Key(
                type = emptyList(),
                name = "Public Key",
                id = "$userUrl/pubkey",
                owner = userUrl,
                publicKeyPem = userAuthEntity.publicKey
            )
        )
    }

    suspend fun receiveFollow(follow: Follow) {
        val actor = follow.actor ?: throw IllegalArgumentException("actor is null")
        val person = webFingerService.fetchUserModel(actor) ?: throw IllegalArgumentException("actor is not found")
        val inboxUrl = person.inbox ?: throw IllegalArgumentException("inbox is not found")
        httpClient.postAp(
            inboxUrl, person.preferredUsername!!, Accept(
                name = "Follow",
                `object` = follow,
                actor = follow.`object`.orEmpty()
            )
        )
    }
}
