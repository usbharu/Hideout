package dev.usbharu.hideout.service.signature

import dev.usbharu.hideout.plugins.KtorKeyMap
import dev.usbharu.hideout.repository.IUserRepository
import io.ktor.http.*
import tech.barbero.http.message.signing.SignatureHeaderVerifier

class HttpSignatureVerifyServiceImpl(private val userAuthService: IUserRepository) : HttpSignatureVerifyService {
    override fun verify(headers: Headers): Boolean {
        val build = SignatureHeaderVerifier.builder().keyMap(KtorKeyMap(userAuthService)).build()
        return true
//        build.verify(object : HttpMessage {
//            override fun headerValues(name: String?): MutableList<String> {
//                return name?.let { headers.getAll(it) }?.toMutableList() ?: mutableListOf()
//            }
//
//            override fun addHeader(name: String?, value: String?) {
//                TODO()
//            }
//
//        })
    }
}
