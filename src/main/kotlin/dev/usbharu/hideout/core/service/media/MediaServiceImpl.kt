package dev.usbharu.hideout.core.service.media

import dev.usbharu.hideout.core.domain.exception.media.MediaFileSizeIsZeroException
import dev.usbharu.hideout.core.domain.exception.media.MediaSaveException
import dev.usbharu.hideout.core.domain.exception.media.UnsupportedMediaException
import dev.usbharu.hideout.core.domain.model.media.MediaRepository
import dev.usbharu.hideout.core.service.media.converter.MediaProcessService
import dev.usbharu.hideout.mastodon.interfaces.api.media.MediaRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import javax.imageio.ImageIO
import dev.usbharu.hideout.core.domain.model.media.Media as EntityMedia

@Service
@Suppress("TooGenericExceptionCaught")
class MediaServiceImpl(
    private val mediaDataStore: MediaDataStore,
    private val fileTypeDeterminationService: FileTypeDeterminationService,
    private val mediaBlurhashService: MediaBlurhashService,
    private val mediaRepository: MediaRepository,
    private val mediaProcessService: MediaProcessService
) : MediaService {
    override suspend fun uploadLocalMedia(mediaRequest: MediaRequest): EntityMedia {
        logger.info(
            "Media upload. filename:${mediaRequest.file.name} size:${mediaRequest.file.size} " +
                    "contentType:${mediaRequest.file.contentType}"
        )

        if (mediaRequest.file.size == 0L) {
            throw MediaFileSizeIsZeroException("Media file size is zero.")
        }

        val fileType = fileTypeDeterminationService.fileType(
            mediaRequest.file.bytes,
            mediaRequest.file.name,
            mediaRequest.file.contentType
        )
        if (fileType != FileType.Image) {
            throw UnsupportedMediaException("FileType: $fileType  is not supported.")
        }

        val process = mediaProcessService.process(
            fileType,
            mediaRequest.file.contentType.orEmpty(),
            mediaRequest.file.name,
            mediaRequest.file.bytes,
            mediaRequest.thumbnail?.bytes
        )

        val dataMediaSave = MediaSave(
            "${UUID.randomUUID()}.${process.file.extension}",
            "",
            process.file.byteArray,
            process.thumbnail?.byteArray
        )
        val save = try {
            mediaDataStore.save(dataMediaSave)
        } catch (e: Exception) {
            logger.warn("Failed save media", e)
            throw MediaSaveException("Failed save media.", e)
        }

        if (save.success.not()) {
            save as FaildSavedMedia
            logger.warn("Failed save media. reason: ${save.reason}")
            logger.warn(save.description, save.trace)
            throw MediaSaveException("Failed save media.")
        }
        save as SuccessSavedMedia

        val blurHash = withContext(Dispatchers.IO) {
            mediaBlurhashService.generateBlurhash(ImageIO.read(mediaRequest.file.bytes.inputStream()))
        }

        return mediaRepository.save(
            EntityMedia(
                id = mediaRepository.generateId(),
                name = mediaRequest.file.name,
                url = save.url,
                remoteUrl = null,
                thumbnailUrl = save.thumbnailUrl,
                type = fileType,
                blurHash = blurHash
            )
        )
    }

    override suspend fun uploadRemoteMedia(remoteMedia: RemoteMedia) = Unit

    companion object {
        private val logger = LoggerFactory.getLogger(MediaServiceImpl::class.java)
    }
}
