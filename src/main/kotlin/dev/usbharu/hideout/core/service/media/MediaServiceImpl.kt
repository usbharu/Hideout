package dev.usbharu.hideout.core.service.media

import dev.usbharu.hideout.core.domain.exception.media.MediaSaveException
import dev.usbharu.hideout.core.domain.exception.media.UnsupportedMediaException
import dev.usbharu.hideout.core.domain.model.media.Media
import dev.usbharu.hideout.core.domain.model.media.MediaRepository
import dev.usbharu.hideout.core.service.media.converter.MediaProcessService
import dev.usbharu.hideout.mastodon.interfaces.api.media.MediaRequest
import dev.usbharu.hideout.util.withDelete
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Files
import javax.imageio.ImageIO
import dev.usbharu.hideout.core.domain.model.media.Media as EntityMedia

@Service
@Suppress("TooGenericExceptionCaught")
open class MediaServiceImpl(
    private val mediaDataStore: MediaDataStore,
    private val fileTypeDeterminationService: FileTypeDeterminationService,
    private val mediaBlurhashService: MediaBlurhashService,
    private val mediaRepository: MediaRepository,
    private val mediaProcessServices: List<MediaProcessService>,
    private val remoteMediaDownloadService: RemoteMediaDownloadService,
    private val renameService: MediaFileRenameService
) : MediaService {
    override suspend fun uploadLocalMedia(mediaRequest: MediaRequest): EntityMedia {
        val fileName = mediaRequest.file.name
        logger.info(
            "Media upload. filename:$fileName " +
                    "contentType:${mediaRequest.file.contentType}"
        )

        val tempFile = Files.createTempFile("hideout-tmp-file", ".tmp")



        tempFile.withDelete().use {
            Files.newOutputStream(tempFile).use { outputStream ->
                mediaRequest.file.inputStream.use {
                    it.transferTo(outputStream)
                }
            }
            val mimeType = fileTypeDeterminationService.fileType(tempFile, fileName)

            val process = findMediaProcessor(mimeType).process(
                mimeType,
                fileName,
                tempFile,
                null
            )

            val dataMediaSave = MediaSaveRequest(
                renameService.rename(
                    mediaRequest.file.name,
                    mimeType,
                    process.filePath.fileName.toString(),
                    process.fileMimeType
                ),
                "",
                process.filePath,
                process.thumbnailPath
            )
            dataMediaSave.filePath.withDelete().use {
                dataMediaSave.thumbnailPath.withDelete().use {
                    val save = try {
                        mediaDataStore.save(dataMediaSave)
                    } catch (e: Exception) {
                        logger.warn("Failed to save the media", e)
                        throw MediaSaveException("Failed to save the media.", e)
                    }
                    if (save.success.not()) {
                        save as FaildSavedMedia
                        logger.warn("Failed to save the media. reason: ${save.reason}")
                        logger.warn(save.description, save.trace)
                        throw MediaSaveException("Failed to save the media.")
                    }
                    save as SuccessSavedMedia
                    val blurHash = generateBlurhash(process)
                    return mediaRepository.save(
                        EntityMedia(
                            id = mediaRepository.generateId(),
                            name = fileName,
                            url = save.url,
                            remoteUrl = null,
                            thumbnailUrl = save.thumbnailUrl,
                            type = process.fileMimeType.fileType,
                            mimeType = process.fileMimeType,
                            blurHash = blurHash,
                            description = mediaRequest.description
                        )
                    )
                }
            }
        }


    }

    // TODO: 仮の処理として保存したように動かす
    override suspend fun uploadRemoteMedia(remoteMedia: RemoteMedia): Media {
        logger.info("MEDIA Remote media. filename:${remoteMedia.name} url:${remoteMedia.url}")

        remoteMediaDownloadService.download(remoteMedia.url).withDelete().use {
            val mimeType = fileTypeDeterminationService.fileType(it.path, remoteMedia.name)

            val process = findMediaProcessor(mimeType).process(mimeType, remoteMedia.name, it.path, null)

            val mediaSaveRequest = MediaSaveRequest(
                renameService.rename(
                    remoteMedia.name,
                    mimeType,
                    process.filePath.fileName.toString(),
                    process.fileMimeType
                ),
                "",
                process.filePath,
                process.thumbnailPath
            )

            mediaSaveRequest.filePath.withDelete().use {
                mediaSaveRequest.filePath.withDelete().use {
                    val save = try {
                        mediaDataStore.save(mediaSaveRequest)
                    } catch (e: Exception) {
                        logger.warn("Failed to save the media", e)
                        throw MediaSaveException("Failed to save the media.", e)
                    }

                    if (save is FaildSavedMedia) {
                        logger.warn("Failed to save the media. reason: ${save.reason}")
                        logger.warn(save.description, save.trace)
                        throw MediaSaveException("Failed to save the media.")
                    }
                    save as SuccessSavedMedia
                    val blurhash = generateBlurhash(process)
                    return mediaRepository.save(
                        EntityMedia(
                            id = mediaRepository.generateId(),
                            name = remoteMedia.name,
                            url = save.url,
                            remoteUrl = remoteMedia.url,
                            thumbnailUrl = save.thumbnailUrl,
                            type = process.fileMimeType.fileType,
                            mimeType = process.fileMimeType,
                            blurHash = blurhash
                        )
                    )
                }
            }
        }
    }

    protected fun findMediaProcessor(mimeType: MimeType): MediaProcessService {
        try {
            return mediaProcessServices.first {
                try {
                    it.isSupport(mimeType)
                } catch (e: Exception) {
                    false
                }
            }
        } catch (e: NoSuchElementException) {
            throw UnsupportedMediaException("MediaType: $mimeType isn't supported.")
        }
    }

    protected fun generateBlurhash(process: ProcessedMediaPath): String {
        val path = if (process.thumbnailPath != null && process.thumbnailMimeType != null) {
            process.thumbnailPath
        } else {
            process.filePath
        }
        val mimeType = if (process.thumbnailPath != null && process.thumbnailMimeType != null) {
            process.thumbnailMimeType
        } else {
            process.fileMimeType
        }

        val imageReadersByMIMEType = ImageIO.getImageReadersByMIMEType(mimeType.type + "/" + mimeType.subtype)
        for (imageReader in imageReadersByMIMEType) {
            try {
                val bufferedImage = ImageIO.createImageInputStream(path.toFile()).use {
                    imageReader.input = it
                    imageReader.read(0)
                }
                return mediaBlurhashService.generateBlurhash(bufferedImage)
            } catch (e: Exception) {
                logger.warn("Failed to read thumbnail", e)
            }
        }
        return ""
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MediaServiceImpl::class.java)
    }
}
