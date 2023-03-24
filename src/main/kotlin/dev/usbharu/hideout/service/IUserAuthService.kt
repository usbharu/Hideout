package dev.usbharu.hideout.service

interface IUserAuthService {
    fun hash(password:String): String

    suspend fun usernameAlreadyUse(username: String):Boolean
    suspend fun registerAccount(username: String, hash: String)

    suspend fun verifyAccount(username: String,password: String): Boolean
}
