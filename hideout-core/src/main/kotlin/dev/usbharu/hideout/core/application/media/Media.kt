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

import dev.usbharu.hideout.core.domain.model.media.FileType
import dev.usbharu.hideout.core.domain.model.media.Media
import dev.usbharu.hideout.core.domain.model.media.MimeType
import java.net.URI

data class Media(
    val id: Long,
    val name: String,
    val url: URI,
    val thumbprintURI: URI?,
    val remoteURL: URI?,
    val type: FileType,
    val mimeType: MimeType,
    val blurHash: String?,
    val description: String?
) {
    companion object {
        fun of(media: Media): dev.usbharu.hideout.core.application.media.Media {
            return Media(
                id = media.id.id,
                name = media.name.name,
                url = media.url,
                thumbprintURI = media.thumbnailUrl,
                remoteURL = media.remoteUrl,
                type = media.type,
                mimeType = media.mimeType,
                blurHash = media.blurHash?.hash,
                description = media.description?.description
            )
        }
    }
}
