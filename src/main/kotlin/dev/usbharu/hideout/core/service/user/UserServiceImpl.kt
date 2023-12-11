package dev.usbharu.hideout.core.service.user

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.user.User
import dev.usbharu.hideout.core.domain.model.user.UserRepository
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.instance.InstanceService
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userAuthService: UserAuthService,
    private val userQueryService: UserQueryService,
    private val userBuilder: User.UserBuilder,
    private val applicationConfig: ApplicationConfig,
    private val instanceService: InstanceService
) :
    UserService {

    override suspend fun usernameAlreadyUse(username: String): Boolean {
        val findByNameAndDomain = userQueryService.findByNameAndDomain(username, applicationConfig.url.host)
        return findByNameAndDomain != null
    }

    override suspend fun createLocalUser(user: UserCreateDto): User {
        val nextId = userRepository.nextId()
        val hashedPassword = userAuthService.hash(user.password)
        val keyPair = userAuthService.generateKeyPair()
        val userUrl = "${applicationConfig.url}/users/${user.name}"
        val userEntity = userBuilder.of(
            id = nextId,
            name = user.name,
            domain = applicationConfig.url.host,
            screenName = user.screenName,
            description = user.description,
            password = hashedPassword,
            inbox = "$userUrl/inbox",
            outbox = "$userUrl/outbox",
            url = userUrl,
            publicKey = keyPair.public.toPem(),
            privateKey = keyPair.private.toPem(),
            createdAt = Instant.now(),
            following = "$userUrl/following",
            followers = "$userUrl/followers",
            keyId = "$userUrl#pubkey"
        )
        return userRepository.save(userEntity)
    }

    @Transactional
    override suspend fun createRemoteUser(user: RemoteUserCreateDto): User {
        logger.info("START Create New remote user. name: {} url: {}", user.name, user.url)
        @Suppress("TooGenericExceptionCaught")
        val instance = try {
            instanceService.fetchInstance(user.url, user.sharedInbox)
        } catch (e: Exception) {
            logger.warn("FAILED to fetch instance. url: {}", user.url, e)
            null
        }

        val nextId = userRepository.nextId()
        val userEntity = userBuilder.of(
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
            instance = instance?.id
        )
        return try {
            val save = userRepository.save(userEntity)
            logger.warn("SUCCESS Create New remote user. id: {} name: {} url: {}", userEntity.id, user.name, user.url)
            save
        } catch (_: ExposedSQLException) {
            logger.warn("FAILED User already exists. name: {} url: {}", user.name, user.url)
            userQueryService.findByUrl(user.url)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }
}
