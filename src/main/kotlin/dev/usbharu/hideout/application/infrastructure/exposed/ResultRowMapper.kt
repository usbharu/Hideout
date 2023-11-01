package dev.usbharu.hideout.application.infrastructure.exposed

import org.jetbrains.exposed.sql.ResultRow

interface ResultRowMapper<T> {
    fun map(resultRow: ResultRow): T
}
