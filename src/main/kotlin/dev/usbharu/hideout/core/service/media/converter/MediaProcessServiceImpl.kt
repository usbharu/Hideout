package dev.usbharu.hideout.core.service.media.converter

import dev.usbharu.hideout.core.domain.exception.media.MediaConvertException
import dev.usbharu.hideout.core.service.media.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Path

@Service
@Suppress("TooGenericExceptionCaught")
class MediaProcessServiceImpl(
    private val mediaConverterRoot: MediaConverterRoot,
    private val thumbnailGenerateService: ThumbnailGenerateService
) : MediaProcessService {
    override fun isSupport(mimeType: MimeType): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun process(
        fileType: FileType,
        contentType: String,
        filename: String,
        file: ByteArray,
        thumbnail: ByteArray?
    ): ProcessedMedia {
        val fileInputStream = try {
            mediaConverterRoot.convert(fileType, contentType, filename, file.inputStream().buffered())
        } catch (e: Exception) {
            logger.warn("Failed convert media.", e)
            throw MediaConvertException("Failed convert media.", e)
        }
        val thumbnailInputStream = try {
            thumbnail?.let { mediaConverterRoot.convert(fileType, contentType, filename, it.inputStream().buffered()) }
        } catch (e: Exception) {
            logger.warn("Failed convert thumbnail media.", e)
            null
        }
        return ProcessedMedia(
            fileInputStream,
            thumbnailGenerateService.generate(
                thumbnailInputStream?.byteArray ?: file,
                2048,
                2048
            )
        )
    }

    override suspend fun process(
        mimeType: MimeType,
        fileName: String,
        filePath: Path,
        thumbnails: Path?
    ): ProcessedMediaPath {
        TODO("Not yet implemented")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MediaProcessServiceImpl::class.java)
    }
}
