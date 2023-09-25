package dev.usbharu.hideout.service.auth

import dev.usbharu.hideout.config.ApplicationConfig
import dev.usbharu.hideout.plugins.KtorKeyMap
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.core.Transaction
import io.ktor.http.*
import org.springframework.stereotype.Service
import tech.barbero.http.message.signing.SignatureHeaderVerifier

@Service
interface HttpSignatureVerifyService {
    fun verify(headers: Headers): Boolean
}

@Service
class HttpSignatureVerifyServiceImpl(
    private val userQueryService: UserQueryService,
    private val transaction: Transaction,
    private val applicationConfig: ApplicationConfig
) : HttpSignatureVerifyService {
    override fun verify(headers: Headers): Boolean {
        val build =
            SignatureHeaderVerifier.builder().keyMap(KtorKeyMap(userQueryService, transaction, applicationConfig))
                .build()
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
