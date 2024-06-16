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

package dev.usbharu.hideout.core.application.media

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.CommandExecutor
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.media.*
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import dev.usbharu.hideout.core.external.media.MediaProcessor
import dev.usbharu.hideout.core.external.mediastore.MediaStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import dev.usbharu.hideout.core.domain.model.media.Media as MediaModel

@Service
class UploadMediaApplicationService(
    @Qualifier("delegate") private val mediaProcessor: MediaProcessor,
    private val mediaStore: MediaStore,
    private val mediaRepository: MediaRepository,
    private val idGenerateService: IdGenerateService,
    transaction: Transaction
) : AbstractApplicationService<UploadMedia, Media>(
    transaction, logger
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UploadMediaApplicationService::class.java)
    }

    override suspend fun internalExecute(command: UploadMedia, executor: CommandExecutor): Media {
        val process = mediaProcessor.process(command.path, command.name, null)
        val id = idGenerateService.generateId()
        val thumbnailUri = if (process.thumbnailPath != null) {
            mediaStore.upload(process.thumbnailPath, "thumbnail-$id.${process.mimeType.subtype}")
        } else {
            null
        }
        val uri = mediaStore.upload(process.path, "$id.${process.mimeType.subtype}")

        val media = MediaModel(
            MediaId(id),
            MediaName(command.name),
            uri,
            command.remoteUri,
            thumbnailUri,
            process.fileType,
            process.mimeType,
            process.blurHash?.let { MediaBlurHash(it) },
            command.description?.let { MediaDescription(it) }
        )

        mediaRepository.save(media)

        return Media.of(media)

    }
}