package dev.usbharu.hideout.service.media.converter

import dev.usbharu.hideout.domain.model.hideout.dto.FileType
import dev.usbharu.hideout.exception.media.MediaConvertException
import dev.usbharu.hideout.service.media.ThumbnailGenerateService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MediaProcessServiceImpl(
    private val mediaConverterRoot: MediaConverterRoot,
    private val thumbnailGenerateService: ThumbnailGenerateService
) : MediaProcessService {
    override suspend fun process(
        fileType: FileType,
        file: ByteArray,
        thumbnail: ByteArray?
    ): Pair<ByteArray, ByteArray> {

        val fileInputStream = try {
            mediaConverterRoot.convert(fileType, file.inputStream().buffered())
        } catch (e: Exception) {
            logger.warn("Failed convert media.", e)
            throw MediaConvertException("Failed convert media.", e)
        }
        val thumbnailInputStream = try {
            thumbnail?.let { mediaConverterRoot.convert(fileType, it.inputStream().buffered()) }
        } catch (e: Exception) {
            logger.warn("Failed convert thumbnail media.", e)
            null
        }
        return fileInputStream to thumbnailGenerateService.generate(
            thumbnailInputStream ?: fileInputStream,
            2048,
            2048
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MediaProcessServiceImpl::class.java)
    }
}
