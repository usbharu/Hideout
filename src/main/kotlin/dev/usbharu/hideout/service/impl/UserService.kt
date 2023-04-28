package dev.usbharu.hideout.service.impl

import dev.usbharu.hideout.domain.model.User
import dev.usbharu.hideout.exception.UserNotFoundException
import dev.usbharu.hideout.repository.IUserRepository
import java.lang.Integer.min

class UserService(private val userRepository: IUserRepository) {

    private val maxLimit = 100
    suspend fun findAll(limit: Int? = maxLimit, offset: Long? = 0): List<User> {

        return userRepository.findAllByLimitAndByOffset(
            min(limit ?: maxLimit, maxLimit),
            offset ?: 0
        )
    }

    suspend fun findById(id: Long): User {
        return userRepository.findById(id) ?: throw UserNotFoundException("$id was not found.")
    }

    suspend fun findByIds(ids: List<Long>): List<User> {
        return userRepository.findByIds(ids)
    }

    suspend fun findByName(name: String): User {
        return userRepository.findByName(name)
            ?: throw UserNotFoundException("$name was not found.")
    }

    suspend fun findByNameAndDomains(names: List<Pair<String,String>>): List<User> {
        return userRepository.findByNameAndDomains(names)
    }

    suspend fun findByUrl(url: String): User {
        return userRepository.findByUrl(url) ?: throw UserNotFoundException("$url was not found.")
    }

    suspend fun findByUrls(urls: List<String>): List<User> {
        return userRepository.findByUrls(urls)
    }

    suspend fun create(user: User): User {
        return userRepository.save(user)
    }

    suspend fun findFollowersById(id: Long): List<User> {
        return userRepository.findFollowersById(id)
    }

    suspend fun addFollowers(id: Long, follower: Long) {
        return userRepository.createFollower(id, follower)
    }

}
