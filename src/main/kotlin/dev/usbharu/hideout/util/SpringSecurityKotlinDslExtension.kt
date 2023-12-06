package dev.usbharu.hideout.util

import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.config.annotation.web.AuthorizeHttpRequestsDsl
import org.springframework.security.web.access.intercept.RequestAuthorizationContext

fun AuthorizeHttpRequestsDsl.hasScope(scope: String): AuthorizationManager<RequestAuthorizationContext> =
    hasAuthority("SCOPE_$scope")

@Suppress("SpreadOperator")
fun AuthorizeHttpRequestsDsl.hasAnyScope(vararg scopes: String): AuthorizationManager<RequestAuthorizationContext> =
    hasAnyAuthority(*scopes.map { "SCOPE_$it" }.toTypedArray())
