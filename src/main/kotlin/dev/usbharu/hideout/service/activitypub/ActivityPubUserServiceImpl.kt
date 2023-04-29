package dev.usbharu.hideout.service.activitypub

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.ap.Image
import dev.usbharu.hideout.domain.model.ap.Key
import dev.usbharu.hideout.domain.model.ap.Person
import dev.usbharu.hideout.domain.model.hideout.dto.RemoteUserCreateDto
import dev.usbharu.hideout.exception.UserNotFoundException
import dev.usbharu.hideout.exception.ap.IllegalActivityPubObjectException
import dev.usbharu.hideout.plugins.getAp
import dev.usbharu.hideout.service.impl.IUserService
import dev.usbharu.hideout.util.HttpUtil.Activity
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class ActivityPubUserServiceImpl(
    private val userService: IUserService,
    private val httpClient: HttpClient
) :
    ActivityPubUserService {

    override suspend fun getPersonByName(name: String): Person {
        // TODO: JOINで書き直し
        val userEntity = userService.findByNameLocalUser(name)
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

    override suspend fun fetchPerson(url: String, targetActor: String?): Person {
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
            val httpResponse = if (targetActor != null) {
                httpClient.getAp(url, "$targetActor#pubkey")
            } else {
                httpClient.get(url) {
                    accept(ContentType.Application.Activity)
                }
            }
            val person = Config.configData.objectMapper.readValue<Person>(httpResponse.bodyAsText())

            userService.createRemoteUser(
                RemoteUserCreateDto(
                    name = person.preferredUsername
                        ?: throw IllegalActivityPubObjectException("preferredUsername is null"),
                    domain = url.substringAfter("://").substringBeforeLast("/"),
                    screenName = (person.name ?: person.preferredUsername)
                        ?: throw IllegalActivityPubObjectException("preferredUsername is null"),
                    description = person.summary.orEmpty(),
                    inbox = person.inbox ?: throw IllegalActivityPubObjectException("inbox is null"),
                    outbox = person.outbox ?: throw IllegalActivityPubObjectException("outbox is null"),
                    url = url,
                    publicKey = person.publicKey?.publicKeyPem
                        ?: throw IllegalActivityPubObjectException("publicKey is null"),
                )
            )
            person
        }
    }
}
