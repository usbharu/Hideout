package dev.usbharu.hideout.core.infrastructure.springframework

import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.model.support.principal.PrincipalContextHolder
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.HideoutUserDetails
import dev.usbharu.hideout.core.query.principal.PrincipalQueryService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component("formLogin")
class SpringSecurityFormLoginPrincipalContextHolder(
    private val transaction: Transaction,
    private val principalQueryService: PrincipalQueryService
) : PrincipalContextHolder {
    override suspend fun getPrincipal(): Principal {
        val hideoutUserDetails =
            SecurityContextHolder.getContext().authentication?.principal as? HideoutUserDetails ?: return Anonymous

        return transaction.transaction {

            val userDetail = principalQueryService.findByUserDetailId(UserDetailId(hideoutUserDetails.userDetailsId))
            LocalUser(
                userDetail.actorId,
                userDetail.userDetailId,
                Acct(userDetail.username, userDetail.host)
            )
        }

    }
}