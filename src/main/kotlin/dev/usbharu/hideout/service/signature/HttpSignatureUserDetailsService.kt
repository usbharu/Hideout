package dev.usbharu.hideout.service.signature

import dev.usbharu.hideout.exception.FailedToGetResourcesException
import dev.usbharu.hideout.exception.HttpSignatureVerifyException
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.core.Transaction
import dev.usbharu.hideout.util.RsaUtil
import dev.usbharu.httpsignature.common.HttpRequest
import dev.usbharu.httpsignature.common.PublicKey
import dev.usbharu.httpsignature.verify.FailedVerification
import dev.usbharu.httpsignature.verify.HttpSignatureVerifier
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken

class HttpSignatureUserDetailsService(
    private val userQueryService: UserQueryService,
    private val httpSignatureVerifier: HttpSignatureVerifier,
    private val transaction: Transaction
) :
    AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
    override fun loadUserDetails(token: PreAuthenticatedAuthenticationToken): UserDetails = runBlocking {
        if (token.principal !is String) {
            throw IllegalStateException("Token is not String")
        }
        if (token.credentials !is HttpRequest) {
            throw IllegalStateException("Credentials is not HttpRequest")
        }

        val keyId = token.principal as String
        val findByKeyId = transaction.transaction {
            try {
                userQueryService.findByKeyId(keyId)
            } catch (e: FailedToGetResourcesException) {
                throw UsernameNotFoundException("User not found", e)
            }
        }

        @Suppress("TooGenericExceptionCaught")
        val verify = try {
            httpSignatureVerifier.verify(
                token.credentials as HttpRequest,
                PublicKey(RsaUtil.decodeRsaPublicKeyPem(findByKeyId.publicKey), keyId)
            )
        } catch (e: RuntimeException) {
            throw BadCredentialsException("", e)
        }

        if (verify is FailedVerification) {
            logger.warn("FAILED Verify HTTP Signature reason: {}", verify.reason)
            throw HttpSignatureVerifyException(verify.reason)
        }

        HttpSignatureUser(
            username = findByKeyId.name,
            domain = findByKeyId.domain,
            id = findByKeyId.id,
            credentialsNonExpired = true,
            accountNonLocked = true,
            authorities = mutableListOf()
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HttpSignatureUserDetailsService::class.java)
    }
}
