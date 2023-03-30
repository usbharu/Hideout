package dev.usbharu.hideout.service

import dev.usbharu.hideout.domain.model.UserEntity
import dev.usbharu.hideout.webfinger.WebFinger

interface IWebFingerService {
    suspend fun fetch(acct:String): WebFinger?

    suspend fun sync(webFinger: WebFinger):UserEntity

    suspend fun fetchAndSync(acct: String):UserEntity{
        val webFinger = fetch(acct)?: throw IllegalArgumentException()
        return sync(webFinger)
    }
}
