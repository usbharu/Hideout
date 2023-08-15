package dev.usbharu.hideout.service.user

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.dto.RemoteUserCreateDto
import dev.usbharu.hideout.domain.model.hideout.dto.SendFollowDto
import dev.usbharu.hideout.domain.model.hideout.dto.UserCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.exception.UserNotFoundException
import dev.usbharu.hideout.query.FollowerQueryService
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.repository.UserRepository
import dev.usbharu.hideout.service.ap.APSendFollowService
import org.koin.core.annotation.Single
import java.time.Instant

@Single
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userAuthService: UserAuthService,
    private val apSendFollowService: APSendFollowService,
    private val userQueryService: UserQueryService,
    private val followerQueryService: FollowerQueryService
) :
    UserService {

    override suspend fun usernameAlreadyUse(username: String): Boolean {
        val findByNameAndDomain = userQueryService.findByNameAndDomain(username, Config.configData.domain)
        return findByNameAndDomain != null
    }

    override suspend fun createLocalUser(user: UserCreateDto): User {
        val nextId = userRepository.nextId()
        val hashedPassword = userAuthService.hash(user.password)
        val keyPair = userAuthService.generateKeyPair()
        val userEntity = User.of(
            id = nextId,
            name = user.name,
            domain = Config.configData.domain,
            screenName = user.screenName,
            description = user.description,
            password = hashedPassword,
            inbox = "${Config.configData.url}/users/${user.name}/inbox",
            outbox = "${Config.configData.url}/users/${user.name}/outbox",
            url = "${Config.configData.url}/users/${user.name}",
            publicKey = keyPair.public.toPem(),
            privateKey = keyPair.private.toPem(),
            createdAt = Instant.now()
        )
        return userRepository.save(userEntity)
    }

    override suspend fun createRemoteUser(user: RemoteUserCreateDto): User {
        val nextId = userRepository.nextId()
        val userEntity = User.of(
            id = nextId,
            name = user.name,
            domain = user.domain,
            screenName = user.screenName,
            description = user.description,
            inbox = user.inbox,
            outbox = user.outbox,
            url = user.url,
            publicKey = user.publicKey,
            createdAt = Instant.now()
        )
        return userRepository.save(userEntity)
    }

    // TODO APのフォロー処理を作る
    override suspend fun followRequest(id: Long, followerId: Long): Boolean {
        val user = userRepository.findById(id) ?: throw UserNotFoundException("$id was not found.")
        val follower = userRepository.findById(followerId) ?: throw UserNotFoundException("$followerId was not found.")
        return if (user.domain == Config.configData.domain) {
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
