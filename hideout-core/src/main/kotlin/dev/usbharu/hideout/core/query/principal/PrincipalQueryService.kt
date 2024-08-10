package dev.usbharu.hideout.core.query.principal

import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId

interface PrincipalQueryService {
    suspend fun findByUserDetailId(userDetailId: UserDetailId): PrincipalDTO
}