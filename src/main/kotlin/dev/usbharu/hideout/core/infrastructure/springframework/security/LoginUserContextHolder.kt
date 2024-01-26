package dev.usbharu.hideout.core.infrastructure.springframework.security

interface LoginUserContextHolder {
    fun getLoginUserId(): Long
}
