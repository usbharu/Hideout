package dev.usbharu.hideout.service

import dev.usbharu.hideout.ap.Image
import dev.usbharu.hideout.ap.Key
import dev.usbharu.hideout.ap.Person
import dev.usbharu.hideout.config.Config
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*

class ActivityPubUserService(
    private val httpClient:HttpClient,
    private val userService: UserService,
    private val userAuthService: IUserAuthService
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

    suspend fun fetchUserModel(url:String):Person? {
        return try {
            httpClient.get(url).body<Person>()
        } catch (e: ResponseException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                return null
            }
            throw e
        }
    }

    suspend fun receiveFollow(){

    }
}
