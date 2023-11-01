package dev.usbharu.hideout.core.service.media

import java.io.InputStream

interface ThumbnailGenerateService {
    fun generate(bufferedImage: InputStream, width: Int, height: Int): ProcessedFile?
    fun generate(outputStream: ByteArray, width: Int, height: Int): ProcessedFile?
}
