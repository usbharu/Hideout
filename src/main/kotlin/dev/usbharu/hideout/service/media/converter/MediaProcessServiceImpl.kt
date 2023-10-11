package dev.usbharu.hideout.service.media.converter

import dev.usbharu.hideout.domain.model.hideout.dto.FileType
import dev.usbharu.hideout.domain.model.hideout.dto.ProcessedMedia
import dev.usbharu.hideout.exception.media.MediaConvertException
import dev.usbharu.hideout.service.media.ThumbnailGenerateService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
@Suppress("TooGenericExceptionCaught")
class MediaProcessServiceImpl(
    private val mediaConverterRoot: MediaConverterRoot,
    private val thumbnailGenerateService: ThumbnailGenerateService
) : MediaProcessService {
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

    companion object {
        private val logger = LoggerFactory.getLogger(MediaProcessServiceImpl::class.java)
    }
}
