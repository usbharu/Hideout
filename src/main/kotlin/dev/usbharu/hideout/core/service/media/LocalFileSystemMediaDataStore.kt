package dev.usbharu.hideout.core.service.media

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.config.LocalStorageConfig
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream

/**
 * ローカルファイルシステムにメディアを保存します
 *
 * @constructor
 * ApplicationConfigとLocalStorageConfigをもとに作成
 *
 * @param applicationConfig ApplicationConfig
 * @param localStorageConfig LocalStorageConfig
 */
@Service
@ConditionalOnProperty("hideout.storage.type", havingValue = "local", matchIfMissing = true)
class LocalFileSystemMediaDataStore(
    applicationConfig: ApplicationConfig,
    localStorageConfig: LocalStorageConfig
) : MediaDataStore {

    private val savePath: Path = Path.of(localStorageConfig.path).toAbsolutePath()

    private val publicUrl = localStorageConfig.publicUrl ?: "${applicationConfig.url}/files/"

    init {
        savePath.createDirectories()
    }

    @Suppress("NestedBlockDepth")
    override suspend fun save(dataMediaSave: MediaSave): SavedMedia {
        val fileSavePath = buildSavePath(savePath, dataMediaSave.name)
        val thumbnailSavePath = buildSavePath(savePath, "thumbnail-" + dataMediaSave.name)

        dataMediaSave.thumbnailInputStream?.inputStream()?.use {
            it.buffered().use { bufferedInputStream ->
                thumbnailSavePath.outputStream(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
                    .use { outputStream ->
                        outputStream.buffered().use {
                            bufferedInputStream.transferTo(it)
                        }
                    }
            }
        }

        dataMediaSave.fileInputStream.inputStream().use {
            it.buffered().use { bufferedInputStream ->
                fileSavePath.outputStream(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
                    .use { outputStream -> outputStream.buffered().use { bufferedInputStream.transferTo(it) } }
            }
        }

        return SuccessSavedMedia(
            dataMediaSave.name,
            publicUrl + dataMediaSave.name,
            publicUrl + "thumbnail-" + dataMediaSave.name
        )
    }

    override suspend fun save(dataSaveRequest: MediaSaveRequest): SavedMedia {
        logger.info("START Media upload. {}", dataSaveRequest.name)
        val fileSavePath = buildSavePath(savePath, dataSaveRequest.name)
        val thumbnailSavePath = buildSavePath(savePath, "thumbnail-" + dataSaveRequest.name)

        val fileSavePathString = fileSavePath.toAbsolutePath().toString()
        logger.info("MEDIA save. path: {}", fileSavePathString)

        @Suppress("TooGenericExceptionCaught")
        try {
            dataSaveRequest.filePath.copyTo(fileSavePath)
            dataSaveRequest.thumbnailPath?.copyTo(thumbnailSavePath)
        } catch (e: Exception) {
            logger.warn("FAILED to Save the media.", e)
            return FaildSavedMedia("FAILED to Save the media.", "Failed copy to path: $fileSavePathString", e)
        }

        logger.info("SUCCESS Media upload. {}", dataSaveRequest.name)
        return SuccessSavedMedia(
            dataSaveRequest.name,
            publicUrl + dataSaveRequest.name,
            publicUrl + "thumbnail-" + dataSaveRequest.name
        )
    }

    /**
     * メディアを削除します。サムネイルも削除されます。
     *
     * @param id 削除するメディアのid [SuccessSavedMedia.name]を指定します。
     */
    override suspend fun delete(id: String) {
        logger.info("START Media delete. id: {}", id)
        @Suppress("TooGenericExceptionCaught")
        try {
            buildSavePath(savePath, id).deleteIfExists()
            buildSavePath(savePath, "thumbnail-$id").deleteIfExists()
        } catch (e: Exception) {
            logger.warn("FAILED Media delete. id: {}", id, e)
        }
    }

    private fun buildSavePath(savePath: Path, name: String): Path = savePath.resolve(name)

    companion object {
        private val logger = LoggerFactory.getLogger(LocalFileSystemMediaDataStore::class.java)
    }
}
