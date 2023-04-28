package dev.usbharu.hideout.service.activitypub

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.domain.model.ap.Image
import dev.usbharu.hideout.domain.model.ap.Key
import dev.usbharu.hideout.domain.model.ap.Person
import dev.usbharu.hideout.exception.UserNotFoundException
import dev.usbharu.hideout.exception.ap.IllegalActivityPubObjectException
import dev.usbharu.hideout.service.IUserAuthService
import dev.usbharu.hideout.service.impl.UserService
import dev.usbharu.hideout.util.HttpUtil.Activity
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.slf4j.LoggerFactory
import java.time.Instant

class ActivityPubUserServiceImpl(
    private val userService: UserService,
    private val userAuthService: IUserAuthService,
    private val httpClient: HttpClient
) :
    ActivityPubUserService {

        private val logger = LoggerFactory.getLogger(this::class.java)
    override suspend fun getPersonByName(name: String): Person {
        // TODO: JOINで書き直し
        val userEntity = userService.findByName(name)
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
                id = "$userUrl#pubkey",
                owner = userUrl,
                publicKeyPem = userEntity.publicKey
            )
        )
    }

    override suspend fun fetchPerson(url: String): Person {
        return try {
            val userEntity = userService.findByUrl(url)
            return Person(
                type = emptyList(),
                name = userEntity.name,
                id = url,
                preferredUsername = userEntity.name,
                summary = userEntity.description,
                inbox = "$url/inbox",
                outbox = "$url/outbox",
                url = url,
                icon = Image(
                    type = emptyList(),
                    name = "$url/icon.png",
                    mediaType = "image/png",
                    url = "$url/icon.png"
                ),
                publicKey = Key(
                    type = emptyList(),
                    name = "Public Key",
                    id = "$url#pubkey",
                    owner = url,
                    publicKeyPem = userEntity.publicKey
                )
            )

        } catch (e: UserNotFoundException) {
            val httpResponse = httpClient.get(url) {
                accept(ContentType.Application.Activity)
            }
            val person = Config.configData.objectMapper.readValue<Person>(httpResponse.bodyAsText())
            userService.create(
                User(
                    id = 0L,
                    name = person.preferredUsername
                        ?: throw IllegalActivityPubObjectException("preferredUsername is null"),
                    domain = url.substringAfter(":").substringBeforeLast("/"),
                    screenName = (person.name ?: person.preferredUsername) ?: throw IllegalActivityPubObjectException("preferredUsername is null"),
                    description = person.summary ?: "",
                    inbox = person.inbox ?: throw IllegalActivityPubObjectException("inbox is null"),
                    outbox = person.outbox ?: throw IllegalActivityPubObjectException("outbox is null"),
                    url = url,
                    publicKey = person.publicKey?.publicKeyPem ?: throw IllegalActivityPubObjectException("publicKey is null"),
                    createdAt = Instant.now()
                )
            )
            person
        }

    }
}
