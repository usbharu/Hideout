package dev.usbharu.hideout.service.media

import dev.usbharu.hideout.domain.model.MediaSave
import dev.usbharu.hideout.domain.model.hideout.form.Media
import dev.usbharu.hideout.exception.media.MediaException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import javax.imageio.ImageIO

@Service
class MediaServiceImpl(
    private val mediaDataStore: MediaDataStore,
    private val fileTypeDeterminationService: FileTypeDeterminationService,
    private val mediaBlurhashService: MediaBlurhashService
) : MediaService {
    override suspend fun uploadLocalMedia(media: Media): SavedMedia {
        if (media.file.size == 0L) {
            return FaildSavedMedia(
                "File size is 0.",
                "Cannot upload a file with a file size of 0."
            )
        }

        val fileType = fileTypeDeterminationService.fileType(media.file.bytes, media.file.name, media.file.contentType)
        if (fileType != FileTypeDeterminationService.FileType.Image) {
            return FaildSavedMedia("Unsupported file type.", "FileType: $fileType is not supported.")
        }

        try {
            mediaDataStore.save(
                MediaSave(
                    media.file.name,
                    "",
                    media.file.inputStream,
                    media.thumbnail.inputStream
                )
            )
        } catch (e: MediaException) {
            return FaildSavedMedia(
                "Faild to upload.",
                e.localizedMessage,
                e
            )
        }

        val withContext = withContext(Dispatchers.IO) {
            mediaBlurhashService.generateBlurhash(ImageIO.read(media.file.inputStream))
        }

        return SuccessSavedMedia(
            media.file.name, "", "",
            withContext
        )
    }
}
