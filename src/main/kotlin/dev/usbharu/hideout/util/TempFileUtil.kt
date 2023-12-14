package dev.usbharu.hideout.util

import java.nio.file.Files
import java.nio.file.Path

fun <T : Path?> T.withDelete(): TempFile<T> = TempFile(this)

class TempFile<T : Path?>(val path: T) : AutoCloseable {
    override fun close() {
        path?.let { Files.deleteIfExists(it) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TempFile<*>

        return path == other.path
    }

    override fun hashCode(): Int {
        return path?.hashCode() ?: 0
    }
}
