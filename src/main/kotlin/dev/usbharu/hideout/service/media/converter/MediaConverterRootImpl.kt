package dev.usbharu.hideout.service.media.converter

import dev.usbharu.hideout.domain.model.hideout.dto.FileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class MediaConverterRootImpl(private val converters: List<MediaConverter>) : MediaConverterRoot {
    override suspend fun convert(fileType: FileType, inputStream: InputStream): ByteArray {
        return converters.find {
            it.isSupport(fileType)
        }?.convert(inputStream) ?: withContext(Dispatchers.IO) {
            inputStream.readAllBytes()
        }
    }
}
