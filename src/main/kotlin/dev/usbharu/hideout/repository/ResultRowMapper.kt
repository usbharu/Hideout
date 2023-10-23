package dev.usbharu.hideout.repository

import org.jetbrains.exposed.sql.ResultRow

interface ResultRowMapper<T> {
    fun map(resultRow: ResultRow): T
}
