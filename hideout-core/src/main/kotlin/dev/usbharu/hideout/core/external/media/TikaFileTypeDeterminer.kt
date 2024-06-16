package dev.usbharu.hideout.core.external.media

import dev.usbharu.hideout.core.domain.model.media.FileType
import dev.usbharu.hideout.core.domain.model.media.MimeType
import org.apache.tika.Tika
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class TikaFileTypeDeterminer : FileTypeDeterminer {
    override fun fileType(path: Path, filename: String): MimeType {
        logger.info("START Detect file type name: {}", filename)

        val tika = Tika()

        val detect = try {
            tika.detect(path)
        } catch (e: IllegalStateException) {
            logger.warn("FAILED Detect file type", e)
            "application/octet-stream"
        }

        val type = detect.substringBefore("/")
        val fileType = when (type) {
            "image" -> {
                FileType.Image
            }

            "video" -> {
                FileType.Video
            }

            "audio" -> {
                FileType.Audio
            }

            else -> {
                FileType.Unknown
            }
        }
        val mimeType = MimeType(type, detect.substringAfter("/"), fileType)

        logger.info("SUCCESS Detect file type name: {},MimeType: {}", filename, mimeType)
        return mimeType
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TikaFileTypeDeterminer::class.java)
    }
}