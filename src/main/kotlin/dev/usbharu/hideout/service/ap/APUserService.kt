package dev.usbharu.hideout.service.ap

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.ApplicationConfig
import dev.usbharu.hideout.domain.model.ap.Image
import dev.usbharu.hideout.domain.model.ap.Key
import dev.usbharu.hideout.domain.model.ap.Person
import dev.usbharu.hideout.domain.model.hideout.dto.RemoteUserCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.exception.FailedToGetResourcesException
import dev.usbharu.hideout.exception.ap.IllegalActivityPubObjectException
import dev.usbharu.hideout.plugins.getAp
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.core.Transaction
import dev.usbharu.hideout.service.user.UserService
import dev.usbharu.hideout.util.HttpUtil.Activity
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

interface APUserService {
    suspend fun getPersonByName(name: String): Person

    /**
     * Fetch person
     *
     * @param url
     * @param targetActor 署名するユーザー
     * @return
     */
    suspend fun fetchPerson(url: String, targetActor: String? = null): Person

    suspend fun fetchPersonWithEntity(url: String, targetActor: String? = null): Pair<Person, User>
}

@Service
class APUserServiceImpl(
    private val userService: UserService,
    private val httpClient: HttpClient,
    private val userQueryService: UserQueryService,
    private val transaction: Transaction,
    private val applicationConfig: ApplicationConfig,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper
) :
    APUserService {

    override suspend fun getPersonByName(name: String): Person {
        val userEntity = transaction.transaction {
            userQueryService.findByNameAndDomain(name, applicationConfig.url.host)
        }
        // TODO: JOINで書き直し
        val userUrl = "${applicationConfig.url}/users/$name"
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
            ),
            endpoints = mapOf("sharedInbox" to "${applicationConfig.url}/inbox")
        )
    }

    override suspend fun fetchPerson(url: String, targetActor: String?): Person =
        fetchPersonWithEntity(url, targetActor).first

    override suspend fun fetchPersonWithEntity(url: String, targetActor: String?): Pair<Person, User> {
        return try {
            val userEntity = userQueryService.findByUrl(url)
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
                ),
                endpoints = mapOf("sharedInbox" to "${applicationConfig.url}/inbox")
            ) to userEntity
        } catch (ignore: FailedToGetResourcesException) {
            val httpResponse = if (targetActor != null) {
                httpClient.getAp(url, "$targetActor#pubkey")
            } else {
                httpClient.get(url) {
                    accept(ContentType.Application.Activity)
                }
            }
            val person = objectMapper.readValue<Person>(httpResponse.bodyAsText())

            person to userService.createRemoteUser(
                RemoteUserCreateDto(
                    name = person.preferredUsername
                        ?: throw IllegalActivityPubObjectException("preferredUsername is null"),
                    domain = url.substringAfter("://").substringBefore("/"),
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
        }
    }
}
