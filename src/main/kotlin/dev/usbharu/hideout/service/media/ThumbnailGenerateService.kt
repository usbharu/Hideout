package dev.usbharu.hideout.service.media

import java.io.InputStream

interface ThumbnailGenerateService {
    fun generate(bufferedImage: InputStream, width: Int, height: Int): ByteArray
    fun generate(outputStream: ByteArray, width: Int, height: Int): ByteArray
}
