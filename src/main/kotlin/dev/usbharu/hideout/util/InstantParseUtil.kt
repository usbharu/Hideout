package dev.usbharu.hideout.util

import java.time.Instant
import java.time.format.DateTimeParseException

object InstantParseUtil {
    fun parse(str: String?): Instant? {
        return try {
            Instant.ofEpochMilli(str?.toLong() ?: return null)
        } catch (e: NumberFormatException) {
            try {
                Instant.parse(str)
            } catch (e: DateTimeParseException) {
                null
            }
        }
    }
}
