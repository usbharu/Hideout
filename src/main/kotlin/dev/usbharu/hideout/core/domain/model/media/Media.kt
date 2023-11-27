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
