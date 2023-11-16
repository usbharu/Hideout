package dev.usbharu.hideout.core.service.media

import java.nio.file.Path

interface RemoteMediaDownloadService {
    suspend fun download(url: String): Path
}
