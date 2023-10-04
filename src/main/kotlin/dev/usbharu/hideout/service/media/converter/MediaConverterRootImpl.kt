package dev.usbharu.hideout.service.media.converter

import dev.usbharu.hideout.domain.model.hideout.dto.FileType
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

@Service
class MediaConverterRootImpl(private val converters: List<MediaConverter>) : MediaConverterRoot {
    override suspend fun convert(fileType: FileType, inputStream: InputStream): OutputStream {
        return converters.find {
            it.isSupport(fileType)
        }?.convert(inputStream) ?: inputStream.let {
            val byteArrayOutputStream = ByteArrayOutputStream()
            it.transferTo(byteArrayOutputStream)
            byteArrayOutputStream
        }
    }
}
