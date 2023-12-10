package dev.usbharu.hideout.core.service.user

import dev.usbharu.hideout.core.domain.model.user.User
import org.springframework.stereotype.Service

@Service
interface UserService {

    suspend fun usernameAlreadyUse(username: String): Boolean

    suspend fun createLocalUser(user: UserCreateDto): User

    suspend fun createRemoteUser(user: RemoteUserCreateDto): User
}
