package dev.usbharu.hideout.core.infrastructure.springframework.oauth2

import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.principal.FromApi
import dev.usbharu.hideout.core.domain.model.support.principal.PrincipalContextHolder
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.query.principal.PrincipalQueryService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class SpringSecurityOauth2PrincipalContextHolder(private val principalQueryService: PrincipalQueryService) :
    PrincipalContextHolder {
    override suspend fun getPrincipal(): FromApi {
        val principal = SecurityContextHolder.getContext().authentication?.principal as Jwt

        val id = principal.getClaim<String>("uid").toLong()
        val userDetail = principalQueryService.findByUserDetailId(UserDetailId(id))

        return FromApi(
            userDetail.actorId,
            userDetail.userDetailId,
            Acct(userDetail.username, userDetail.host)
        )
    }
}