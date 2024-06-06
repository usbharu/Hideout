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

import dev.usbharu.hideout.core.application.actor.RegisterLocalActor
import dev.usbharu.hideout.core.application.actor.RegisterLocalActorApplicationService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

@Controller
class AuthController(private val registerLocalActorApplicationService: RegisterLocalActorApplicationService) {
    @GetMapping("/auth/sign_up")
    fun signUp(): String {
        return "sign_up"
    }

    @PostMapping("/auth/sign_up")
    suspend fun signUp(@Validated @ModelAttribute signUpForm: SignUpForm, request: HttpServletRequest): String {
        val registerLocalActor = RegisterLocalActor(signUpForm.username, signUpForm.password)
        val uri = registerLocalActorApplicationService.register(registerLocalActor)
        request.login(signUpForm.username, signUpForm.password)
        return "redirect:$uri"
    }
}
