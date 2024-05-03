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

package dev.usbharu.hideout.mastodon.interfaces.api.instance

import dev.usbharu.hideout.controller.mastodon.generated.InstanceApi
import dev.usbharu.hideout.domain.mastodon.model.generated.V1Instance
import dev.usbharu.hideout.mastodon.service.instance.InstanceApiService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class MastodonInstanceApiController(private val instanceApiService: InstanceApiService) : InstanceApi {
    override suspend fun apiV1InstanceGet(): ResponseEntity<V1Instance> =
        ResponseEntity(instanceApiService.v1Instance(), HttpStatus.OK)
}
