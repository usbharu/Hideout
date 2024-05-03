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

package dev.usbharu.hideout.core.service.user

import dev.usbharu.hideout.core.domain.model.actor.Actor
import org.springframework.stereotype.Service

@Service
interface UserService {

    suspend fun usernameAlreadyUse(username: String): Boolean

    suspend fun createLocalUser(user: UserCreateDto): Actor

    suspend fun createRemoteUser(user: RemoteUserCreateDto): Actor

    suspend fun updateUser(userId: Long, updateUserDto: UpdateUserDto)

    suspend fun deleteRemoteActor(actorId: Long)

    suspend fun deleteLocalUser(userId: Long)

    suspend fun updateUserStatistics(userId: Long)
}
