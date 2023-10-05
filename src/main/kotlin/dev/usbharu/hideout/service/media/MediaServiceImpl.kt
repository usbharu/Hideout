package dev.usbharu.hideout.service.media

import dev.usbharu.hideout.domain.model.MediaSave
import dev.usbharu.hideout.domain.model.hideout.dto.FaildSavedMedia
import dev.usbharu.hideout.domain.model.hideout.dto.FileType
import dev.usbharu.hideout.domain.model.hideout.dto.RemoteMedia
import dev.usbharu.hideout.domain.model.hideout.dto.SuccessSavedMedia
import dev.usbharu.hideout.domain.model.hideout.form.Media
import dev.usbharu.hideout.exception.media.MediaFileSizeIsZeroException
import dev.usbharu.hideout.exception.media.MediaSaveException
import dev.usbharu.hideout.exception.media.UnsupportedMediaException
import dev.usbharu.hideout.repository.MediaRepository
import dev.usbharu.hideout.service.media.converter.MediaProcessService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import javax.imageio.ImageIO
import dev.usbharu.hideout.domain.model.hideout.entity.Media as EntityMedia

@Service
class MediaServiceImpl(
    private val mediaDataStore: MediaDataStore,
    private val fileTypeDeterminationService: FileTypeDeterminationService,
    private val mediaBlurhashService: MediaBlurhashService,
    private val mediaRepository: MediaRepository,
    private val mediaProcessService: MediaProcessService
) : MediaService {
    override suspend fun uploadLocalMedia(media: Media): EntityMedia {
        logger.info(
            "Media upload. filename:${media.file.name} size:${media.file.size} contentType:${media.file.contentType}"
        )

        if (media.file.size == 0L) {
            throw MediaFileSizeIsZeroException("Media file size is zero.")
        }

        val fileType = fileTypeDeterminationService.fileType(media.file.bytes, media.file.name, media.file.contentType)
        if (fileType != FileType.Image) {
            throw UnsupportedMediaException("FileType: $fileType  is not supported.")
        }

        val process = mediaProcessService.process(
            fileType,
            media.file.contentType.orEmpty(),
            media.file.name,
            media.file.bytes,
            media.thumbnail?.bytes
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
            mediaBlurhashService.generateBlurhash(ImageIO.read(media.file.bytes.inputStream()))
        }

        return mediaRepository.save(
            EntityMedia(
                id = mediaRepository.generateId(),
                name = media.file.name,
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
