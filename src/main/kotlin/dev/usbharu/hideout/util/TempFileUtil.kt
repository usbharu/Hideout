package dev.usbharu.hideout.util

import java.nio.file.Files
import java.nio.file.Path

fun Path?.withDelete(): TempFile = TempFile(this)

class TempFile(val path: Path?) : AutoCloseable {
    override fun close() {
        path?.let { Files.deleteIfExists(it) }
    }
}
