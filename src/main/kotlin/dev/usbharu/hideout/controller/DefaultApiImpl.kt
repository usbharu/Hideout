package dev.usbharu.hideout.controller

import dev.usbharu.hideout.controller.generated.DefaultApi
import dev.usbharu.hideout.domain.model.hideout.dto.JwtToken
import dev.usbharu.hideout.service.api.UserAuthApiService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class DefaultApiImpl(private val userAuthApiService: UserAuthApiService) : DefaultApi {
    override fun refreshTokenPost(): ResponseEntity<JwtToken> {
        return ResponseEntity(HttpStatus.OK)
    }
}
