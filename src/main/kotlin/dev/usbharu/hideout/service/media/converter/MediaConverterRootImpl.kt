package dev.usbharu.hideout.service.media.converter

import dev.usbharu.hideout.domain.model.hideout.dto.FileType
import dev.usbharu.hideout.domain.model.hideout.dto.ProcessedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class MediaConverterRootImpl(private val converters: List<MediaConverter>) : MediaConverterRoot {
    override suspend fun convert(
        fileType: FileType,
        contentType: String,
        filename: String,
        inputStream: InputStream
    ): ProcessedFile {
        val convert = converters.find {
            it.isSupport(fileType)
        }?.convert(inputStream)
        if (convert != null) {
            return convert
        }
        return withContext(Dispatchers.IO) {
            if (filename.contains('.')) {
                ProcessedFile(inputStream.readAllBytes(), filename.substringAfterLast("."))
            } else {
                ProcessedFile(inputStream.readAllBytes(), contentType.substringAfterLast("/"))
            }
        }
    }
}
