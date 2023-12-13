package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.deletedActor.DeletedActor
import dev.usbharu.hideout.core.domain.model.deletedActor.DeletedActorRepository
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import org.springframework.stereotype.Repository

@Repository
class DeletedActorRepositoryImpl : DeletedActorRepository {
    override suspend fun save(deletedActor: DeletedActor): DeletedActor {
        val singleOrNull = DeletedActors.select { DeletedActors.id eq deletedActor.id }.singleOrNull()

        if (singleOrNull == null) {
            DeletedActors.insert {
                it[DeletedActors.id] = deletedActor.id
                it[DeletedActors.name] = deletedActor.name
                it[DeletedActors.domain] = deletedActor.domain
                it[DeletedActors.publicKey] = deletedActor.publicKey
                it[DeletedActors.deletedAt] = deletedActor.deletedAt
            }
        } else {
            DeletedActors.update({ DeletedActors.id eq deletedActor.id }) {
                it[DeletedActors.name] = deletedActor.name
                it[DeletedActors.domain] = deletedActor.domain
                it[DeletedActors.publicKey] = deletedActor.publicKey
                it[DeletedActors.deletedAt] = deletedActor.deletedAt
            }
        }
        return deletedActor
    }

    override suspend fun delete(deletedActor: DeletedActor) {
        DeletedActors.deleteWhere { DeletedActors.id eq deletedActor.id }
    }

    override suspend fun findById(id: Long): DeletedActor {
        val singleOr = DeletedActors.select { DeletedActors.id eq id }
            .singleOr { FailedToGetResourcesException("id: $id was not exist or duplicate", it) }

        return deletedActor(singleOr)
    }
}

fun ResultRow.toDeletedActor(): DeletedActor = deletedActor(this)

private fun deletedActor(singleOr: ResultRow): DeletedActor {
    return DeletedActor(
        singleOr[DeletedActors.id],
        singleOr[DeletedActors.name],
        singleOr[DeletedActors.domain],
        singleOr[DeletedActors.publicKey],
        singleOr[DeletedActors.deletedAt]
    )
}

object DeletedActors : Table("deleted_actors") {
    val id = long("id")
    val name = varchar("name", 300)
    val domain = varchar("domain", 255)
    val publicKey = varchar("public_key", 10000).uniqueIndex()
    val deletedAt = timestamp("deleted_at")
    override val primaryKey: PrimaryKey = PrimaryKey(id)

    init {
        uniqueIndex(name, domain)
    }
}
