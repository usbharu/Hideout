package dev.usbharu.hideout.core.domain.model.instance

interface InstanceRepository {
    suspend fun generateId(): Long
    suspend fun save(instance: Instance): Instance
    suspend fun findById(id: Long): Instance?
    suspend fun delete(instance: Instance)
}
