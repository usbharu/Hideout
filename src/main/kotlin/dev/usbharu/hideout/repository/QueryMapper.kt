package dev.usbharu.hideout.repository

import org.jetbrains.exposed.sql.Query

interface QueryMapper<T> {
    fun map(query: Query): List<T>
}
