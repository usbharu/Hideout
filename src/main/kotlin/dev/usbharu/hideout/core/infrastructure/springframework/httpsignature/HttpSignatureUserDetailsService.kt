package dev.usbharu.hideout.core.infrastructure.springframework.httpsignature

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.exception.HttpSignatureVerifyException
import dev.usbharu.hideout.core.query.ActorQueryService
import dev.usbharu.hideout.util.RsaUtil
import dev.usbharu.httpsignature.common.HttpMethod
import dev.usbharu.httpsignature.common.HttpRequest
import dev.usbharu.httpsignature.common.PublicKey
import dev.usbharu.httpsignature.verify.FailedVerification
import dev.usbharu.httpsignature.verify.HttpSignatureVerifier
import dev.usbharu.httpsignature.verify.SignatureHeaderParser
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken

class HttpSignatureUserDetailsService(
    private val actorQueryService: ActorQueryService,
    private val httpSignatureVerifier: HttpSignatureVerifier,
    private val transaction: Transaction,
    private val httpSignatureHeaderParser: SignatureHeaderParser
) :
    AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
    override fun loadUserDetails(token: PreAuthenticatedAuthenticationToken): UserDetails = runBlocking {
        check(token.principal is String) { "Token is not String" }
        val credentials = token.credentials

        check(credentials is HttpRequest) { "Credentials is not HttpRequest" }

        val keyId = token.principal as String
        val findByKeyId = transaction.transaction {
            try {
                actorQueryService.findByKeyId(keyId)
            } catch (e: FailedToGetResourcesException) {
                throw UsernameNotFoundException("User not found", e)
            }
        }

        val signature = httpSignatureHeaderParser.parse(credentials.headers)

        val requiredHeaders = when (credentials.method) {
            HttpMethod.GET -> getRequiredHeaders
            HttpMethod.POST -> postRequiredHeaders
        }
        if (signature.headers.containsAll(requiredHeaders).not()) {
            logger.warn(
                "FAILED Verify HTTP Signature. required headers: {} but actual: {}",
                requiredHeaders,
                signature.headers
            )
            throw BadCredentialsException("HTTP Signature. required headers: $requiredHeaders")
        }

        @Suppress("TooGenericExceptionCaught")
        val verify = try {
            httpSignatureVerifier.verify(
                credentials,
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
        private val postRequiredHeaders = listOf("(request-target)", "date", "host", "digest")
        private val getRequiredHeaders = listOf("(request-target)", "date", "host")
    }
}
