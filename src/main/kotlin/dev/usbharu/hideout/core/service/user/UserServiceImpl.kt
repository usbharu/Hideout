package dev.usbharu.hideout.core.service.user

import dev.usbharu.hideout.activitypub.service.activity.follow.APSendFollowService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.exception.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.user.User
import dev.usbharu.hideout.core.domain.model.user.UserRepository
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.follow.SendFollowDto
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userAuthService: UserAuthService,
    private val apSendFollowService: APSendFollowService,
    private val userQueryService: UserQueryService,
    private val followerQueryService: FollowerQueryService,
    private val userBuilder: User.UserBuilder,
    private val applicationConfig: ApplicationConfig
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

    override suspend fun createRemoteUser(user: RemoteUserCreateDto): User {
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
            keyId = user.keyId
        )
        return try {
            userRepository.save(userEntity)
        } catch (_: ExposedSQLException) {
            userQueryService.findByUrl(user.url)
        }
    }

    // TODO APのフォロー処理を作る
    override suspend fun followRequest(id: Long, followerId: Long): Boolean {
        val user = userRepository.findById(id) ?: throw UserNotFoundException("$id was not found.")
        val follower = userRepository.findById(followerId) ?: throw UserNotFoundException("$followerId was not found.")
        return if (user.domain == applicationConfig.url.host) {
            follow(id, followerId)
            true
        } else {
            if (userRepository.findFollowRequestsById(id, followerId)) {
                // do-nothing
            } else {
                apSendFollowService.sendFollow(SendFollowDto(follower, user))
            }
            false
        }
    }

    override suspend fun follow(id: Long, followerId: Long) {
        followerQueryService.appendFollower(id, followerId)
        if (userRepository.findFollowRequestsById(id, followerId)) {
            userRepository.deleteFollowRequest(id, followerId)
        }
    }

    override suspend fun unfollow(id: Long, followerId: Long): Boolean {
        followerQueryService.removeFollower(id, followerId)
        return false
    }
}
