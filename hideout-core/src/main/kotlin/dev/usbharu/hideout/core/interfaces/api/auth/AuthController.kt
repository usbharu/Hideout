/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.core.interfaces.api.auth

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.config.CaptchaConfig
import dev.usbharu.hideout.core.service.auth.AuthApiService
import dev.usbharu.hideout.core.service.auth.RegisterAccountDto
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

@Controller
class AuthController(
    private val authApiService: AuthApiService,
    private val captchaConfig: CaptchaConfig,
    private val applicationConfig: ApplicationConfig
) {
    @GetMapping("/auth/sign_up")
    fun signUp(model: Model): String {
        model.addAttribute("siteKey", captchaConfig.reCaptchaSiteKey)
        model.addAttribute("applicationConfig", applicationConfig)
        return "sign_up"
    }

    @PostMapping("/auth/sign_up")
    suspend fun signUp(@Validated @ModelAttribute signUpForm: SignUpForm): String {
        val registerAccount = authApiService.registerAccount(
            RegisterAccountDto(
                signUpForm.username,
                signUpForm.password,
                signUpForm.recaptchaResponse
            )
        )

        return "redirect:" + registerAccount.url
    }
}
