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

import dev.usbharu.hideout.core.application.media.UploadMedia
import dev.usbharu.hideout.core.application.media.UploadMediaApplicationService
import dev.usbharu.hideout.core.domain.model.media.FileType.*
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.Oauth2CommandExecutorFactory
import dev.usbharu.hideout.mastodon.interfaces.api.generated.MediaApi
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.MediaAttachment
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files

@Controller
class SpringMediaApi(
    private val uploadMediaApplicationService: UploadMediaApplicationService,
    private val oauth2CommandExecutorFactory: Oauth2CommandExecutorFactory
) : MediaApi {
    override suspend fun apiV1MediaPost(
        file: MultipartFile,
        thumbnail: MultipartFile?,
        description: String?,
        focus: String?,
    ): ResponseEntity<MediaAttachment> {
        val tempFile = Files.createTempFile("hideout-tmp-file", ".tmp")

        Files.newOutputStream(tempFile).use { outputStream ->
            file.inputStream.use {
                it.transferTo(outputStream)
            }
        }

        val media = uploadMediaApplicationService.execute(
            UploadMedia(
                tempFile,
                file.originalFilename ?: file.name,
                null,
                description
            )
        )

        return ResponseEntity.ok(
            MediaAttachment(
                media.id.toString(),
                when (media.type) {
                    Image -> MediaAttachment.Type.image
                    Video -> MediaAttachment.Type.video
                    Audio -> MediaAttachment.Type.audio
                    Unknown -> MediaAttachment.Type.unknown
                },
                media.url.toString(),
                media.thumbprintURI?.toString(),
                media.remoteURL?.toString(),
                media.description,
                media.blurHash,
                media.url.toASCIIString()
            )
        )
    }
}