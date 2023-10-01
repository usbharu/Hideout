package dev.usbharu.hideout.domain.model.hideout.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded

data class Meta(@Id val version: String, @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY) val jwt: Jwt)
