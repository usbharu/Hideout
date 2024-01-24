package dev.usbharu.hideout.core.service.post

interface PostContentFormatter {
    suspend fun format(content: String): FormattedPostContent
}
