package util

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.infrastructure.springframework.httpsignature.HttpSignatureUser
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.httpsignature.common.HttpHeaders
import dev.usbharu.httpsignature.common.HttpMethod
import dev.usbharu.httpsignature.common.HttpRequest
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import java.net.URL

class WithHttpSignatureSecurityContextFactory(
    private val userQueryService: UserQueryService,
    private val transaction: Transaction
) : WithSecurityContextFactory<WithHttpSignature> {

    private val securityContextStrategy = SecurityContextHolder.getContextHolderStrategy()

    override fun createSecurityContext(annotation: WithHttpSignature): SecurityContext = runBlocking {
        val preAuthenticatedAuthenticationToken = PreAuthenticatedAuthenticationToken(
            annotation.keyId, HttpRequest(
                URL("https://example.com/inbox"),
                HttpHeaders(mapOf()), HttpMethod.GET
            )
        )
        val httpSignatureUser = transaction.transaction {
            val findByKeyId = userQueryService.findByKeyId(annotation.keyId)
            HttpSignatureUser(
                findByKeyId.name,
                findByKeyId.domain,
                findByKeyId.id,
                true,
                true,
                mutableListOf()
            )
        }
        preAuthenticatedAuthenticationToken.details = httpSignatureUser
        preAuthenticatedAuthenticationToken.isAuthenticated = true
        val emptyContext = securityContextStrategy.createEmptyContext()
        emptyContext.authentication = preAuthenticatedAuthenticationToken
        return@runBlocking emptyContext
    }

}
