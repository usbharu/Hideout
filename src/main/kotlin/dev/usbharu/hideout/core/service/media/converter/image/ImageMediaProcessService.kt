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
        val bufferedImage = ImageIO.read(filePath.inputStream())
        val tempFileName = UUID.randomUUID().toString()
        val tempFile = Files.createTempFile(tempFileName, "tmp")

        val thumbnailPath = if (genThumbnail) {
            val tempThumbnailFile = Files.createTempFile("thumbnail-$tempFileName", ".tmp")

            tempThumbnailFile.outputStream().use {
                val write = ImageIO.write(
                    if (thumbnails != null) {
                        Thumbnails.of(thumbnails.toFile()).size(width, height).asBufferedImage()
                    } else {
                        Thumbnails.of(bufferedImage).size(width, height).asBufferedImage()
                    },
                    convertType,
                    it
                )
                if (write) {
                    tempThumbnailFile
                } else {
                    null
                }
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
