package dev.usbharu.hideout.application.infrastructure.exposed

import org.jetbrains.exposed.sql.Query

interface QueryMapper<T> {
    fun map(query: Query): List<T>
}
