package dev.usbharu.hideout.core.infrastructure.springframework.oauth2

import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.model.support.principal.PrincipalContextHolder
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.query.principal.PrincipalQueryService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component("oauth2")
class SpringSecurityOauth2PrincipalContextHolder(
    private val principalQueryService: PrincipalQueryService,
    private val transaction: Transaction
) :
    PrincipalContextHolder {
    override suspend fun getPrincipal(): Principal {
        val principal =
            SecurityContextHolder.getContext().authentication?.principal as? Jwt ?: return Anonymous

        return transaction.transaction {
            val id = principal.getClaim<String>("uid").toLong()
            val userDetail = principalQueryService.findByUserDetailId(UserDetailId(id))

            return@transaction LocalUser(
                userDetail.actorId,
                userDetail.userDetailId,
                Acct(userDetail.username, userDetail.host)
            )
        }
    }
}
