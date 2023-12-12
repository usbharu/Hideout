package dev.usbharu.hideout.core.service.user

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.query.ActorQueryService
import dev.usbharu.hideout.core.service.instance.InstanceService
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class UserServiceImpl(
    private val actorRepository: ActorRepository,
    private val userAuthService: UserAuthService,
    private val actorQueryService: ActorQueryService,
    private val actorBuilder: Actor.UserBuilder,
    private val applicationConfig: ApplicationConfig,
    private val instanceService: InstanceService,
    private val userDetailRepository: UserDetailRepository
) :
    UserService {

    override suspend fun usernameAlreadyUse(username: String): Boolean {
        val findByNameAndDomain = actorQueryService.findByNameAndDomain(username, applicationConfig.url.host)
        return findByNameAndDomain != null
    }

    override suspend fun createLocalUser(user: UserCreateDto): Actor {
        val nextId = actorRepository.nextId()
        val hashedPassword = userAuthService.hash(user.password)
        val keyPair = userAuthService.generateKeyPair()
        val userUrl = "${applicationConfig.url}/users/${user.name}"
        val userEntity = actorBuilder.of(
            id = nextId,
            name = user.name,
            domain = applicationConfig.url.host,
            screenName = user.screenName,
            description = user.description,
            inbox = "$userUrl/inbox",
            outbox = "$userUrl/outbox",
            url = userUrl,
            publicKey = keyPair.public.toPem(),
            privateKey = keyPair.private.toPem(),
            createdAt = Instant.now(),
            following = "$userUrl/following",
            followers = "$userUrl/followers",
            keyId = "$userUrl#pubkey",
            locked = false
        )
        val save = actorRepository.save(userEntity)
        userDetailRepository.save(UserDetail(nextId, hashedPassword, true))
        return save
    }

    @Transactional
    override suspend fun createRemoteUser(user: RemoteUserCreateDto): Actor {
        logger.info("START Create New remote user. name: {} url: {}", user.name, user.url)
        @Suppress("TooGenericExceptionCaught")
        val instance = try {
            instanceService.fetchInstance(user.url, user.sharedInbox)
        } catch (e: Exception) {
            logger.warn("FAILED to fetch instance. url: {}", user.url, e)
            null
        }

        val nextId = actorRepository.nextId()
        val userEntity = actorBuilder.of(
            id = nextId,
            name = user.name,
            domain = user.domain,
            screenName = user.screenName,
            description = user.description,
            inbox = user.inbox,
            outbox = user.outbox,
            url = user.url,
            publicKey = user.publicKey,
            createdAt = Instant.now(),
            followers = user.followers,
            following = user.following,
            keyId = user.keyId,
            instance = instance?.id,
            locked = user.locked ?: false
        )
        return try {
            val save = actorRepository.save(userEntity)
            logger.warn("SUCCESS Create New remote user. id: {} name: {} url: {}", userEntity.id, user.name, user.url)
            save
        } catch (_: ExposedSQLException) {
            logger.warn("FAILED User already exists. name: {} url: {}", user.name, user.url)
            actorQueryService.findByUrl(user.url)
        }
    }

    override suspend fun updateUser(userId: Long, updateUserDto: UpdateUserDto) {
        val userDetail = userDetailRepository.findByActorId(userId)
            ?: throw IllegalArgumentException("userId: $userId was not found.")

        val actor = actorRepository.findById(userId) ?: throw IllegalArgumentException("userId $userId was not found.")

        actorRepository.save(
            actor.copy(
                screenName = updateUserDto.screenName,
                description = updateUserDto.description,
                locked = updateUserDto.locked
            )
        )

        userDetailRepository.save(
            userDetail.copy(
                autoAcceptFolloweeFollowRequest = updateUserDto.autoAcceptFolloweeFollowRequest
            )
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }
}
