package dev.usbharu.hideout.core.service.user

import dev.usbharu.hideout.core.domain.model.actor.Actor
import org.springframework.stereotype.Service

@Service
interface UserService {

    suspend fun usernameAlreadyUse(username: String): Boolean

    suspend fun createLocalUser(user: UserCreateDto): Actor

    suspend fun createRemoteUser(user: RemoteUserCreateDto): Actor

    suspend fun updateUser(userId: Long, updateUserDto: UpdateUserDto)

    suspend fun deleteRemoteActor(actorId: Long)

    suspend fun deleteLocalUser(userId: Long)
}
