package dev.usbharu.hideout.core.infrastructure.localfilesystem

import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.config.LocalStorageConfig
import dev.usbharu.hideout.core.external.mediastore.MediaStore
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.copyTo

@Component
@ConditionalOnProperty("hideout.storage.type", havingValue = "local", matchIfMissing = true)
class LocalFileSystemMediaStore(
    localStorageConfig: LocalStorageConfig,
    applicationConfig: ApplicationConfig
) :
    MediaStore {

    private val publicUrl = localStorageConfig.publicUrl ?: "${applicationConfig.url}/files/"
    override suspend fun upload(path: Path, id: String): URI {
        logger.info("START Media upload. {}", id)
        val fileSavePath = buildSavePath(path, id)

        val fileSavePathString = fileSavePath.toAbsolutePath().toString()
        logger.info("MEDIA save. path: {}", fileSavePathString)

        @Suppress("TooGenericExceptionCaught") try {
            path.copyTo(fileSavePath)
        } catch (e: Exception) {
            logger.warn("FAILED to Save the media.", e)
            throw e
        }

        logger.info("SUCCESS Media upload. {}", id)
        return URI.create(publicUrl).resolve(id)
    }

    private fun buildSavePath(savePath: Path, name: String): Path = savePath.resolve(name)


    companion object {
        private val logger = LoggerFactory.getLogger(LocalFileSystemMediaStore::class.java)
    }

}