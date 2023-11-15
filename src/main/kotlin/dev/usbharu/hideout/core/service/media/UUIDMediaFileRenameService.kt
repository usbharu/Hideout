package dev.usbharu.hideout.core.service.media

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.util.*

@Qualifier("uuid")
@Service
class UUIDMediaFileRenameService : MediaFileRenameService {
    override fun rename(
        uploadName: String,
        uploadMimeType: MimeType,
        processedName: String,
        processedMimeType: MimeType
    ): String = "${UUID.randomUUID()}.${uploadMimeType.subtype}.${processedMimeType.subtype}"
}
