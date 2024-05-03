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

package dev.usbharu.hideout.core.domain.model.media

import dev.usbharu.hideout.core.service.media.FileType
import dev.usbharu.hideout.core.service.media.MimeType
import dev.usbharu.hideout.domain.mastodon.model.generated.MediaAttachment

data class Media(
    val id: Long,
    val name: String,
    val url: String,
    val remoteUrl: String?,
    val thumbnailUrl: String?,
    val type: FileType,
    val mimeType: MimeType,
    val blurHash: String?,
    val description: String? = null
)

fun Media.toMediaAttachments(): MediaAttachment = MediaAttachment(
    id = id.toString(),
    type = when (type) {
        FileType.Image -> MediaAttachment.Type.image
        FileType.Video -> MediaAttachment.Type.video
        FileType.Audio -> MediaAttachment.Type.audio
        FileType.Unknown -> MediaAttachment.Type.unknown
    },
    url = url,
    previewUrl = thumbnailUrl,
    remoteUrl = remoteUrl,
    description = description,
    blurhash = blurHash,
    textUrl = url
)
