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

package dev.usbharu.hideout.activitypub.service.common

import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.core.domain.model.actor.Actor

interface APResourceResolveService {
    suspend fun <T : Object> resolve(url: String, clazz: Class<T>, singer: Actor?): T
    suspend fun <T : Object> resolve(url: String, clazz: Class<T>, singerId: Long?): T
}

suspend inline fun <reified T : Object> APResourceResolveService.resolve(url: String, singer: Actor?): T =
    resolve(url, T::class.java, singer)

suspend inline fun <reified T : Object> APResourceResolveService.resolve(url: String, singerId: Long?): T =
    resolve(url, T::class.java, singerId)
