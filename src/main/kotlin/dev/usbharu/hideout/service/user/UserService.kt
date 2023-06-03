package dev.usbharu.hideout.service.user

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.dto.RemoteUserCreateDto
import dev.usbharu.hideout.domain.model.hideout.dto.SendFollowDto
import dev.usbharu.hideout.domain.model.hideout.dto.UserCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.exception.UserNotFoundException
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.service.activitypub.ActivityPubSendFollowService
import org.koin.core.annotation.Single
import java.lang.Integer.min
import java.time.Instant

@Single
class UserService(
    private val userRepository: IUserRepository,
    private val userAuthService: IUserAuthService,
    private val activityPubSendFollowService: ActivityPubSendFollowService
) :
    IUserService {

    private val maxLimit = 100
    override suspend fun findAll(limit: Int?, offset: Long?): List<User> {
        return userRepository.findAllByLimitAndByOffset(
            min(limit ?: maxLimit, maxLimit),
            offset ?: 0
        )
    }

    override suspend fun findById(id: Long): User =
        userRepository.findById(id) ?: throw UserNotFoundException("$id was not found.")

    override suspend fun findByIds(ids: List<Long>): List<User> = userRepository.findByIds(ids)

    override suspend fun findByName(name: String): List<User> = userRepository.findByName(name)

    override suspend fun findByNameLocalUser(name: String): User {
        return userRepository.findByNameAndDomain(name, Config.configData.domain)
            ?: throw UserNotFoundException("$name was not found.")
    }

    override suspend fun findByNameAndDomain(name: String, domain: String?): User {
        return userRepository.findByNameAndDomain(name, domain ?: Config.configData.domain)
            ?: throw UserNotFoundException("$name was not found.")
    }

    override suspend fun findByNameAndDomains(names: List<Pair<String, String>>): List<User> =
        userRepository.findByNameAndDomains(names)

    override suspend fun findByUrl(url: String): User =
        userRepository.findByUrl(url) ?: throw UserNotFoundException("$url was not found.")

    override suspend fun findByUrls(urls: List<String>): List<User> = userRepository.findByUrls(urls)

    override suspend fun usernameAlreadyUse(username: String): Boolean {
        val findByNameAndDomain = userRepository.findByNameAndDomain(username, Config.configData.domain)
        return findByNameAndDomain != null
    }

    override suspend fun createLocalUser(user: UserCreateDto): User {
        val nextId = userRepository.nextId()
        val hashedPassword = userAuthService.hash(user.password)
        val keyPair = userAuthService.generateKeyPair()
        val userEntity = User(
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
        val userEntity = User(
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

    override suspend fun findFollowersById(id: Long): List<User> = userRepository.findFollowersById(id)
    override suspend fun findFollowersByNameAndDomain(name: String, domain: String?): List<User> {
        TODO("Not yet implemented")
    }

    override suspend fun findFollowingById(id: Long): List<User> {
        TODO("Not yet implemented")
    }

    override suspend fun findFollowingByNameAndDomain(name: String, domain: String?): List<User> {
        TODO("Not yet implemented")
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
                activityPubSendFollowService.sendFollow(SendFollowDto(follower, user))
            }
            false
        }
    }

    override suspend fun follow(id: Long, followerId: Long) {
        userRepository.createFollower(id, followerId)
        if (userRepository.findFollowRequestsById(id, followerId)) {
            userRepository.deleteFollowRequest(id, followerId)
        }
    }

    override suspend fun unfollow(id: Long, followerId: Long): Boolean {
        userRepository.deleteFollower(id, followerId)
        return false
    }
}
