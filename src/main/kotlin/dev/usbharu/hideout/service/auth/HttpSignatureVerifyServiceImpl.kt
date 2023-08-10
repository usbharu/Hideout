package dev.usbharu.hideout.service.auth

import dev.usbharu.hideout.plugins.KtorKeyMap
import dev.usbharu.hideout.query.UserQueryService
import io.ktor.http.*
import org.koin.core.annotation.Single
import tech.barbero.http.message.signing.SignatureHeaderVerifier

@Single
class HttpSignatureVerifyServiceImpl(private val userQueryService: UserQueryService) : HttpSignatureVerifyService {
    override fun verify(headers: Headers): Boolean {
        val build = SignatureHeaderVerifier.builder().keyMap(KtorKeyMap(userQueryService)).build()
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
