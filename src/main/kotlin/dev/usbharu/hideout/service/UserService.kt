package dev.usbharu.hideout.service

import dev.usbharu.hideout.domain.model.User
import dev.usbharu.hideout.domain.model.UserEntity
import dev.usbharu.hideout.exception.UserNotFoundException
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.repository.UserRepository
import org.jetbrains.exposed.sql.Database
import java.lang.Integer.min

class UserService(private val userRepository: IUserRepository) {

    private val maxLimit = 100
    suspend fun findAll(limit: Int? = maxLimit, offset: Long? = 0): List<UserEntity> {

        return userRepository.findAllByLimitAndByOffset(
            min(limit ?: maxLimit, maxLimit),
            offset ?: 0
        )
    }

    suspend fun findById(id: Long): UserEntity {
        return userRepository.findById(id) ?: throw UserNotFoundException("$id was not found.")
    }

    suspend fun findByName(name:String): UserEntity {
        return userRepository.findByName(name) ?: throw UserNotFoundException("$name was not found.")
    }

    suspend fun create(user: User): UserEntity {
        return userRepository.create(user)
    }
}
