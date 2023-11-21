package dev.usbharu.hideout.activitypub.service.objects.user

import dev.usbharu.hideout.activitypub.domain.exception.IllegalActivityPubObjectException
import dev.usbharu.hideout.activitypub.domain.model.Image
import dev.usbharu.hideout.activitypub.domain.model.Key
import dev.usbharu.hideout.activitypub.domain.model.Person
import dev.usbharu.hideout.activitypub.service.common.APResourceResolveService
import dev.usbharu.hideout.activitypub.service.common.resolve
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.user.User
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.user.RemoteUserCreateDto
import dev.usbharu.hideout.core.service.user.UserService
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
    private val userQueryService: UserQueryService,
    private val transaction: Transaction,
    private val applicationConfig: ApplicationConfig,
    private val apResourceResolveService: APResourceResolveService
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
                id = userEntity.keyId,
                owner = userUrl,
                publicKeyPem = userEntity.publicKey
            ),
            endpoints = mapOf("sharedInbox" to "${applicationConfig.url}/inbox"),
            followers = userEntity.followers,
            following = userEntity.following
        )
    }

    override suspend fun fetchPerson(url: String, targetActor: String?): Person =
        fetchPersonWithEntity(url, targetActor).first

    override suspend fun fetchPersonWithEntity(url: String, targetActor: String?): Pair<Person, User> {
        return try {
            val userEntity = userQueryService.findByUrl(url)
            val id = userEntity.url
            return Person(
                type = emptyList(),
                name = userEntity.name,
                id = id,
                preferredUsername = userEntity.name,
                summary = userEntity.description,
                inbox = "$id/inbox",
                outbox = "$id/outbox",
                url = id,
                icon = Image(
                    type = emptyList(),
                    name = "$id/icon.png",
                    mediaType = "image/png",
                    url = "$id/icon.png"
                ),
                publicKey = Key(
                    type = emptyList(),
                    name = "Public Key",
                    id = userEntity.keyId,
                    owner = id,
                    publicKeyPem = userEntity.publicKey
                ),
                endpoints = mapOf("sharedInbox" to "${applicationConfig.url}/inbox"),
                followers = userEntity.followers,
                following = userEntity.following
            ) to userEntity
        } catch (ignore: FailedToGetResourcesException) {
            val person = apResourceResolveService.resolve<Person>(url, null as Long?)

            val id = person.id ?: throw IllegalActivityPubObjectException("id is null")
            person to userService.createRemoteUser(
                RemoteUserCreateDto(
                    name = person.preferredUsername
                        ?: throw IllegalActivityPubObjectException("preferredUsername is null"),
                    domain = id.substringAfter("://").substringBefore("/"),
                    screenName = (person.name ?: person.preferredUsername)
                        ?: throw IllegalActivityPubObjectException("preferredUsername is null"),
                    description = person.summary.orEmpty(),
                    inbox = person.inbox ?: throw IllegalActivityPubObjectException("inbox is null"),
                    outbox = person.outbox ?: throw IllegalActivityPubObjectException("outbox is null"),
                    url = id,
                    publicKey = person.publicKey?.publicKeyPem
                        ?: throw IllegalActivityPubObjectException("publicKey is null"),
                    keyId = person.publicKey?.id ?: throw IllegalActivityPubObjectException("publicKey keyId is null"),
                    following = person.following,
                    followers = person.followers,
                    sharedInbox = person.endpoints["sharedInbox"]
                )
            )
        }
    }
}
