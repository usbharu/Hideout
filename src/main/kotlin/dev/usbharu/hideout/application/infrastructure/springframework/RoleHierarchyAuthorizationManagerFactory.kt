package dev.usbharu.hideout.application.infrastructure.springframework

import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.authorization.AuthorityAuthorizationManager
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.web.access.intercept.RequestAuthorizationContext
import org.springframework.stereotype.Component

@Component
class RoleHierarchyAuthorizationManagerFactory(private val roleHierarchy: RoleHierarchy) {
    fun hasScope(role: String): AuthorizationManager<RequestAuthorizationContext> {
        val hasAuthority = AuthorityAuthorizationManager.hasAuthority<RequestAuthorizationContext>("SCOPE_$role")
        hasAuthority.setRoleHierarchy(roleHierarchy)
        return hasAuthority
    }
}