package dev.usbharu.hideout.core.external.media

import dev.usbharu.hideout.core.domain.model.media.MimeType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
@Qualifier("delegate")
class DelegateMediaProcessor(
    private val fileTypeDeterminer: FileTypeDeterminer,
    private val mediaProcessors: List<MediaProcessor>
) : MediaProcessor {
    override fun isSupported(mimeType: MimeType): Boolean {
        return true
    }

    override suspend fun process(path: Path, filename: String, mimeType: MimeType?): ProcessedMedia {
        val fileType = fileTypeDeterminer.fileType(path, filename)
        return mediaProcessors.first { it.isSupported(fileType) }.process(path, filename, fileType)
    }
}