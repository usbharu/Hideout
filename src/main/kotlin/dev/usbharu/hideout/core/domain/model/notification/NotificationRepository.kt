package dev.usbharu.hideout.core.domain.model.notification

interface NotificationRepository {
    suspend fun generateId(): Long
    suspend fun save(notification: Notification): Notification
    suspend fun findById(id: Long): Notification?
    suspend fun deleteById(id: Long)
}
