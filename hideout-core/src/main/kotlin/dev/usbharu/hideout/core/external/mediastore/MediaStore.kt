package dev.usbharu.hideout.core.external.mediastore

import java.net.URI
import java.nio.file.Path

interface MediaStore {
    suspend fun upload(path: Path, id: String): URI
}
