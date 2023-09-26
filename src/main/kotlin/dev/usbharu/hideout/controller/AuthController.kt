package dev.usbharu.hideout.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AuthController {
    @GetMapping("/auth/sign_up")
    fun signUp(): String = "sign_up"
}
