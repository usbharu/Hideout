package dev.usbharu.hideout.core.service.media.converter

import dev.usbharu.hideout.core.service.media.FileType
import dev.usbharu.hideout.core.service.media.ProcessedFile
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
