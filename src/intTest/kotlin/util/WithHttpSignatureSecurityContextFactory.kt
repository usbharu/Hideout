package util

import dev.usbharu.hideout.core.infrastructure.springframework.httpsignature.HttpSignatureUser
import dev.usbharu.httpsignature.common.HttpHeaders
import dev.usbharu.httpsignature.common.HttpMethod
import dev.usbharu.httpsignature.common.HttpRequest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import java.net.URL

class WithHttpSignatureSecurityContextFactory : WithSecurityContextFactory<WithHttpSignature> {

    private val securityContextStrategy = SecurityContextHolder.getContextHolderStrategy()

    override fun createSecurityContext(annotation: WithHttpSignature): SecurityContext {
        val httpSignatureUser = HttpSignatureUser(
            "user",
            "example.com",
            12345,
            true,
            true,
            mutableListOf()
        )
        val preAuthenticatedAuthenticationToken = PreAuthenticatedAuthenticationToken(
            "user", HttpRequest(
                URL("https://example.com/inbox"),
                HttpHeaders(mapOf()), HttpMethod.GET
            )
        )
        preAuthenticatedAuthenticationToken.details = httpSignatureUser
        preAuthenticatedAuthenticationToken.isAuthenticated = true
        val emptyContext = securityContextStrategy.createEmptyContext()
        emptyContext.authentication = preAuthenticatedAuthenticationToken
        return emptyContext
    }

}
