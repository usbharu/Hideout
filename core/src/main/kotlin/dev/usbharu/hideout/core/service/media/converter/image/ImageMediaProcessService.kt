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

package dev.usbharu.hideout.core.service.media.converter.image

import dev.usbharu.hideout.core.domain.exception.media.MediaProcessException
import dev.usbharu.hideout.core.service.media.FileType
import dev.usbharu.hideout.core.service.media.MimeType
import dev.usbharu.hideout.core.service.media.ProcessedMedia
import dev.usbharu.hideout.core.service.media.ProcessedMediaPath
import dev.usbharu.hideout.core.service.media.converter.MediaProcessService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import net.coobird.thumbnailator.Thumbnails
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.imageio.ImageIO
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@Service
@Qualifier("image")
class ImageMediaProcessService(private val imageMediaProcessorConfiguration: ImageMediaProcessorConfiguration?) :
    MediaProcessService {

    private val convertType = imageMediaProcessorConfiguration?.convert ?: "jpeg"

    private val supportedTypes = imageMediaProcessorConfiguration?.supportedType ?: listOf("webp", "jpeg", "png")

    private val genThumbnail = imageMediaProcessorConfiguration?.thubnail?.generate ?: true

    private val width = imageMediaProcessorConfiguration?.thubnail?.width ?: 1000
    private val height = imageMediaProcessorConfiguration?.thubnail?.height ?: 1000

    override fun isSupport(mimeType: MimeType): Boolean {
        if (mimeType.type != "image") {
            return false
        }
        return supportedTypes.contains(mimeType.subtype)
    }

    override suspend fun process(
        fileType: FileType,
        contentType: String,
        fileName: String,
        file: ByteArray,
        thumbnail: ByteArray?
    ): ProcessedMedia {
        TODO("Not yet implemented")
    }

    override suspend fun process(
        mimeType: MimeType,
        fileName: String,
        filePath: Path,
        thumbnails: Path?
    ): ProcessedMediaPath = withContext(Dispatchers.IO + MDCContext()) {
        val read = ImageIO.read(filePath.inputStream())

        val bufferedImage = BufferedImage(read.width, read.height, BufferedImage.TYPE_INT_RGB)

        val graphics = bufferedImage.createGraphics()

        graphics.drawImage(read, 0, 0, Color.BLACK, null)

        val tempFileName = UUID.randomUUID().toString()
        val tempFile = Files.createTempFile(tempFileName, "tmp")

        val thumbnailPath = if (genThumbnail) {
            val tempThumbnailFile = Files.createTempFile("thumbnail-$tempFileName", ".tmp")

            tempThumbnailFile.outputStream().use {
                val write = ImageIO.write(
                    if (thumbnails != null) {
                        Thumbnails.of(thumbnails.toFile())
                            .size(width, height)
                            .imageType(BufferedImage.TYPE_INT_RGB)
                            .asBufferedImage()
                    } else {
                        Thumbnails.of(bufferedImage)
                            .size(width, height)
                            .imageType(BufferedImage.TYPE_INT_RGB)
                            .asBufferedImage()
                    },
                    convertType,
                    it
                )
                tempThumbnailFile.takeIf { write }
            }
        } else {
            null
        }

        tempFile.outputStream().use {
            if (ImageIO.write(bufferedImage, convertType, it).not()) {
                logger.warn("Failed to save a temporary file. type: {} ,path: {}", convertType, tempFile)
                throw MediaProcessException("Failed to save a temporary file.")
            }
        }
        ProcessedMediaPath(
            tempFile,
            thumbnailPath,
            MimeType("image", convertType, FileType.Image),
            MimeType("image", convertType, FileType.Image).takeIf { genThumbnail }
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ImageMediaProcessService::class.java)
    }
}
