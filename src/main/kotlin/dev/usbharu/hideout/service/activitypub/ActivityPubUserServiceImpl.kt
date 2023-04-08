package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.ap.Image
import dev.usbharu.hideout.ap.Key
import dev.usbharu.hideout.ap.Person
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.service.IUserAuthService
import dev.usbharu.hideout.service.impl.UserService

class ActivityPubUserServiceImpl(private val userService: UserService, private val userAuthService: IUserAuthService) :
    ActivityPubUserService {
    override suspend fun getPersonByName(name: String): Person {
        val userEntity = userService.findByName(name)
        val userAuthEntity = userAuthService.findByUserId(userEntity.id)
        val userUrl = "${Config.configData.url}/users$name"
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
                id = "$userUrl#pubkey",
                owner = userUrl,
                publicKeyPem = userAuthEntity.publicKey
            )
        )
    }
}
