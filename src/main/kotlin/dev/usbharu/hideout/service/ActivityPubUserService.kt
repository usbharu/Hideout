package dev.usbharu.hideout.service

import dev.usbharu.hideout.ap.Image
import dev.usbharu.hideout.ap.Person
import dev.usbharu.hideout.config.Config

class ActivityPubUserService(private val userService: UserService) {
    suspend fun generateUserModel(name:String):Person{
        val userEntity = userService.findByName(name)
        val userUrl = "${Config.configData.hostname}/users/$name"
        return Person(
            emptyList(),
            "Icon",
            userUrl,
            name,
            userEntity.description,
            "$userUrl/inbox",
            "$userUrl/outbox",
            userUrl,
            Image(
                emptyList(),
                "$userUrl/icon.png",
                "image/png",
                "$userUrl/icon.png"
            )
        )
    }
}
