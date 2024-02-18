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

interface APRequestService {
    suspend fun <R : Object> apGet(url: String, signer: Actor? = null, responseClass: Class<R>): R
    suspend fun <T : Object, R : Object> apPost(
        url: String,
        body: T? = null,
        signer: Actor? = null,
        responseClass: Class<R>
    ): R

    suspend fun <T : Object> apPost(url: String, body: T? = null, signer: Actor? = null): String
}

suspend inline fun <reified R : Object> APRequestService.apGet(url: String, signer: Actor? = null): R =
    apGet(url, signer, R::class.java)

suspend inline fun <T : Object, reified R : Object> APRequestService.apPost(
    url: String,
    body: T? = null,
    signer: Actor? = null
): R = apPost(url, body, signer, R::class.java)
