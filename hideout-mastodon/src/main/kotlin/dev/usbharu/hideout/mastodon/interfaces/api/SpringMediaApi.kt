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

package dev.usbharu.hideout.mastodon.interfaces.api

import dev.usbharu.hideout.mastodon.interfaces.api.generated.MediaApi
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.MediaAttachment
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.multipart.MultipartFile

@Controller
class SpringMediaApi : MediaApi {
    override suspend fun apiV1MediaPost(
        file: MultipartFile,
        thumbnail: MultipartFile?,
        description: String?,
        focus: String?,
    ): ResponseEntity<MediaAttachment> {
        return super.apiV1MediaPost(file, thumbnail, description, focus)
    }
}