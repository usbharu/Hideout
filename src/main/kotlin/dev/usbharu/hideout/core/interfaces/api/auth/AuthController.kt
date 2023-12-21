package dev.usbharu.hideout.core.interfaces.api.auth

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AuthController {
    @GetMapping("/auth/sign_up")
    @Suppress("FunctionOnlyReturningConstant")
    fun signUp(): String = "sign_up"
}
