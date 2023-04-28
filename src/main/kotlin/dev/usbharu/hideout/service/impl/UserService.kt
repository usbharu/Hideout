package dev.usbharu.hideout.service.impl

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.dto.RemoteUserCreateDto
import dev.usbharu.hideout.domain.model.hideout.dto.UserCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.exception.UserNotFoundException
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.service.IUserAuthService
import java.lang.Integer.min
import java.time.Instant

class UserService(private val userRepository: IUserRepository, private val userAuthService: IUserAuthService) :
    IUserService {

    private val maxLimit = 100
    override suspend fun findAll(limit: Int?, offset: Long?): List<User> {

        return userRepository.findAllByLimitAndByOffset(
            min(limit ?: maxLimit, maxLimit),
            offset ?: 0
        )
    }

    override suspend fun findById(id: Long): User {
        return userRepository.findById(id) ?: throw UserNotFoundException("$id was not found.")
    }

    override suspend fun findByIds(ids: List<Long>): List<User> {
        return userRepository.findByIds(ids)
    }

    override suspend fun findByName(name: String): List<User> {
        return userRepository.findByName(name)
    }

    override suspend fun findByNameLocalUser(name: String): User {
        return userRepository.findByNameAndDomain(name, Config.configData.domain)
            ?: throw UserNotFoundException("$name was not found.")
    }

    override suspend fun findByNameAndDomains(names: List<Pair<String, String>>): List<User> {
        return userRepository.findByNameAndDomains(names)
    }

    override suspend fun findByUrl(url: String): User {
        return userRepository.findByUrl(url) ?: throw UserNotFoundException("$url was not found.")
    }

    override suspend fun findByUrls(urls: List<String>): List<User> {
        return userRepository.findByUrls(urls)
    }

    override suspend fun usernameAlreadyUse(username: String): Boolean {
        val findByNameAndDomain = userRepository.findByNameAndDomain(username, Config.configData.domain)
        return findByNameAndDomain != null
    }

    override suspend fun createLocalUser(user: UserCreateDto): User {
        val nextId = userRepository.nextId()
        val HashedPassword = userAuthService.hash(user.password)
        val keyPair = userAuthService.generateKeyPair()
        val userEntity = User(
            id = nextId,
            name = user.name,
            domain = Config.configData.domain,
            screenName = user.screenName,
            description = user.description,
            password = HashedPassword,
            inbox = "${Config.configData.url}/users/${user.name}/inbox",
            outbox = "${Config.configData.url}/users/${user.name}/outbox",
            url = "${Config.configData.url}/users/${user.name}",
            publicKey = keyPair.public.toPem(),
            privateKey = keyPair.private.toPem(),
            Instant.now()
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

    override suspend fun findFollowersById(id: Long): List<User> {
        return userRepository.findFollowersById(id)
    }

    override suspend fun addFollowers(id: Long, follower: Long) {
        return userRepository.createFollower(id, follower)
    }

}
