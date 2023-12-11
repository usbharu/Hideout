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
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.query.ActorQueryService
import dev.usbharu.hideout.core.service.user.RemoteUserCreateDto
import dev.usbharu.hideout.core.service.user.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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

    suspend fun fetchPersonWithEntity(url: String, targetActor: String? = null): Pair<Person, Actor>
}

@Service
class APUserServiceImpl(
    private val userService: UserService,
    private val actorQueryService: ActorQueryService,
    private val transaction: Transaction,
    private val applicationConfig: ApplicationConfig,
    private val apResourceResolveService: APResourceResolveService
) :
    APUserService {

    override suspend fun getPersonByName(name: String): Person {
        val userEntity = transaction.transaction {
            actorQueryService.findByNameAndDomain(name, applicationConfig.url.host)
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
                mediaType = "image/jpeg",
                url = "$userUrl/icon.jpg"
            ),
            publicKey = Key(
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

    @Transactional
    override suspend fun fetchPersonWithEntity(url: String, targetActor: String?): Pair<Person, Actor> {
        return try {
            val userEntity = actorQueryService.findByUrl(url)
            val id = userEntity.url
            return entityToPerson(userEntity, id) to userEntity
        } catch (ignore: FailedToGetResourcesException) {
            val person = apResourceResolveService.resolve<Person>(url, null as Long?)

            val id = person.id
            try {
                val userEntity = actorQueryService.findByUrl(id)
                return entityToPerson(userEntity, id) to userEntity
            } catch (_: FailedToGetResourcesException) {
            }
            person to userService.createRemoteUser(
                RemoteUserCreateDto(
                    name = person.preferredUsername
                        ?: throw IllegalActivityPubObjectException("preferredUsername is null"),
                    domain = id.substringAfter("://").substringBefore("/"),
                    screenName = person.name,
                    description = person.summary.orEmpty(),
                    inbox = person.inbox,
                    outbox = person.outbox,
                    url = id,
                    publicKey = person.publicKey.publicKeyPem,
                    keyId = person.publicKey.id,
                    following = person.following,
                    followers = person.followers,
                    sharedInbox = person.endpoints["sharedInbox"]
                )
            )
        }
    }

    private fun entityToPerson(
        actorEntity: Actor,
        id: String
    ) = Person(
        type = emptyList(),
        name = actorEntity.name,
        id = id,
        preferredUsername = actorEntity.name,
        summary = actorEntity.description,
        inbox = "$id/inbox",
        outbox = "$id/outbox",
        url = id,
        icon = Image(
            type = emptyList(),
            mediaType = "image/jpeg",
            url = "$id/icon.jpg"
        ),
        publicKey = Key(
            id = actorEntity.keyId,
            owner = id,
            publicKeyPem = actorEntity.publicKey
        ),
        endpoints = mapOf("sharedInbox" to "${applicationConfig.url}/inbox"),
        followers = actorEntity.followers,
        following = actorEntity.following
    )
}
