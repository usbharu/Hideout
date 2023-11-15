package dev.usbharu.hideout.util

import java.nio.file.Files
import java.nio.file.Path

fun <T : Path?> T.withDelete(): TempFile<T> = TempFile(this)

class TempFile<T : Path?>(val path: T) : AutoCloseable {
    override fun close() {
        path?.let { Files.deleteIfExists(it) }
    }
}
