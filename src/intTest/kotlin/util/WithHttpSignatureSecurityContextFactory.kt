package util

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.infrastructure.springframework.httpsignature.HttpSignatureUserDetailsService
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.httpsignature.common.HttpHeaders
import dev.usbharu.httpsignature.common.HttpMethod
import dev.usbharu.httpsignature.common.HttpRequest
import dev.usbharu.httpsignature.sign.RsaSha256HttpSignatureSigner
import dev.usbharu.httpsignature.verify.DefaultSignatureHeaderParser
import dev.usbharu.httpsignature.verify.RsaSha256HttpSignatureVerifier
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import java.net.URL

class WithHttpSignatureSecurityContextFactory(
    userQueryService: UserQueryService,
    transaction: Transaction
) : WithSecurityContextFactory<WithHttpSignature> {

    private val securityContextStrategy = SecurityContextHolder.getContextHolderStrategy()

    private val httpSignatureUserDetailsService: HttpSignatureUserDetailsService = HttpSignatureUserDetailsService(
        userQueryService,
        RsaSha256HttpSignatureVerifier(DefaultSignatureHeaderParser(), RsaSha256HttpSignatureSigner()),
        transaction
    )


    override fun createSecurityContext(annotation: WithHttpSignature): SecurityContext {
        val preAuthenticatedAuthenticationToken = PreAuthenticatedAuthenticationToken(
            annotation.keyId, HttpRequest(
                URL("https://example.com/inbox"),
                HttpHeaders(mapOf()), HttpMethod.GET
            )
        )
        val httpSignatureUser = httpSignatureUserDetailsService.loadUserDetails(preAuthenticatedAuthenticationToken)
        preAuthenticatedAuthenticationToken.details = httpSignatureUser
        preAuthenticatedAuthenticationToken.isAuthenticated = true
        val emptyContext = securityContextStrategy.createEmptyContext()
        emptyContext.authentication = preAuthenticatedAuthenticationToken
        return emptyContext
    }

}
