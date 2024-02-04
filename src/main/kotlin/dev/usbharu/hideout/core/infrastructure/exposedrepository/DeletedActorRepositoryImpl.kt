package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.model.deletedActor.DeletedActor
import dev.usbharu.hideout.core.domain.model.deletedActor.DeletedActorRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class DeletedActorRepositoryImpl : DeletedActorRepository, AbstractRepository() {
    override val logger: Logger
        get() = Companion.logger

    override suspend fun save(deletedActor: DeletedActor): DeletedActor = query {
        val singleOrNull =
            DeletedActors.selectAll().where { DeletedActors.id eq deletedActor.id }.forUpdate().singleOrNull()

        if (singleOrNull == null) {
            DeletedActors.insert {
                it[id] = deletedActor.id
                it[name] = deletedActor.name
                it[domain] = deletedActor.domain
                it[publicKey] = deletedActor.publicKey
                it[deletedAt] = deletedActor.deletedAt
            }
        } else {
            DeletedActors.update({ DeletedActors.id eq deletedActor.id }) {
                it[name] = deletedActor.name
                it[domain] = deletedActor.domain
                it[publicKey] = deletedActor.publicKey
                it[deletedAt] = deletedActor.deletedAt
            }
        }
        return@query deletedActor
    }

    override suspend fun delete(deletedActor: DeletedActor): Unit = query {
        DeletedActors.deleteWhere { id eq deletedActor.id }
    }

    override suspend fun findById(id: Long): DeletedActor? = query {
        return@query DeletedActors
            .selectAll().where { DeletedActors.id eq id }
            .singleOrNull()
            ?.toDeletedActor()
    }

    override suspend fun findByNameAndDomain(name: String, domain: String): DeletedActor? = query {
        return@query DeletedActors
            .selectAll().where { DeletedActors.name eq name and (DeletedActors.domain eq domain) }
            .singleOrNull()
            ?.toDeletedActor()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DeletedActorRepositoryImpl::class.java)
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
