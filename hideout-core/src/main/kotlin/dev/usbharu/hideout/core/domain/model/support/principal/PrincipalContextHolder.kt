package dev.usbharu.hideout.core.domain.model.support.principal

interface PrincipalContextHolder {
    suspend fun getPrincipal(): Principal
}
