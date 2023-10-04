package dev.usbharu.hideout.service.media

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

interface ThumbnailGenerateService {
    fun generate(bufferedImage: InputStream, width: Int, height: Int): ByteArrayOutputStream
    fun generate(outputStream: OutputStream, width: Int, height: Int): ByteArrayOutputStream
}
