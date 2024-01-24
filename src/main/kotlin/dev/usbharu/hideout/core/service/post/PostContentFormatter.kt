package dev.usbharu.hideout.core.service.post

interface PostContentFormatter {
    fun format(content: String): FormattedPostContent
}
