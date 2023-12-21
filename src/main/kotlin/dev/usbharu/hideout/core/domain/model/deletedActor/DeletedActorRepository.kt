package dev.usbharu.hideout.core.domain.model.deletedActor

interface DeletedActorRepository {
    suspend fun save(deletedActor: DeletedActor): DeletedActor
    suspend fun delete(deletedActor: DeletedActor)
    suspend fun findById(id: Long): DeletedActor?
    suspend fun findByNameAndDomain(name: String, domain: String): DeletedActor?
}
