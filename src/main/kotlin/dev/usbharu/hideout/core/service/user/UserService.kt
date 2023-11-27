package dev.usbharu.hideout.core.service.user

import dev.usbharu.hideout.core.domain.model.user.User
import org.springframework.stereotype.Service

@Service
interface UserService {

    suspend fun usernameAlreadyUse(username: String): Boolean

    suspend fun createLocalUser(user: UserCreateDto): User

    suspend fun createRemoteUser(user: RemoteUserCreateDto): User

    /**
     * フォローリクエストを送信する
     *
     * @param id
     * @param followerId
     * @return リクエストが成功したか
     */
    suspend fun followRequest(id: Long, followerId: Long): Boolean

    /**
     * フォローする
     *
     * @param id
     * @param followerId
     */
    suspend fun follow(id: Long, followerId: Long)

    suspend fun unfollow(id: Long, followerId: Long): Boolean
}
