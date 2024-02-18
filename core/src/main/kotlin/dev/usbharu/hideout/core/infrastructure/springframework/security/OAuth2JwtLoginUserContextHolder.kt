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

package dev.usbharu.hideout.core.infrastructure.springframework.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class OAuth2JwtLoginUserContextHolder : LoginUserContextHolder {
    override fun getLoginUserId(): Long {
        val principal = SecurityContextHolder.getContext().authentication.principal as Jwt

        return principal.getClaim<String>("uid").toLong()
    }

    override fun getLoginUserIdOrNull(): Long? {
        val principal = SecurityContextHolder.getContext()?.authentication?.principal
        if (principal !is Jwt) {
            return null
        }

        return principal.getClaim<String>("uid").toLongOrNull()
    }
}
