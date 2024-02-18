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

package dev.usbharu.hideout.core.infrastructure.springframework.oauth2

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val applicationConfig: ApplicationConfig,
    private val userDetailRepository: UserDetailRepository,
    private val transaction: Transaction,
    private val actorRepository: ActorRepository
) :
    UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails = runBlocking {
        if (username == null) {
            throw UsernameNotFoundException("$username not found")
        }
        transaction.transaction {
            val findById =
                actorRepository.findByNameAndDomain(username, applicationConfig.url.host)
                    ?: throw UserNotFoundException.withNameAndDomain(username, applicationConfig.url.host)

            val userDetails = userDetailRepository.findByActorId(findById.id)
                ?: throw UsernameNotFoundException("${findById.id} not found.")
            UserDetailsImpl(
                id = findById.id,
                username = findById.name,
                password = userDetails.password,
                enabled = true,
                accountNonExpired = true,
                credentialsNonExpired = true,
                accountNonLocked = true,
                authorities = mutableListOf()
            )
        }
    }
}
