package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Jwt
import dev.usbharu.hideout.repository.Meta.id
import dev.usbharu.hideout.repository.Meta.kid
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Single
import java.util.*

@Single
class MetaRepositoryImpl(private val database: Database) : IMetaRepository {

    init {
        transaction(database) {
            SchemaUtils.create(Meta)
            SchemaUtils.createMissingTablesAndColumns(Meta)
        }
    }

    @Suppress("InjectDispatcher")
    override suspend fun <T> transaction(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun save(meta: dev.usbharu.hideout.domain.model.hideout.entity.Meta) {
        if (Meta.select { id eq 1 }.empty()) {
            Meta.insert {
                it[id] = 1
                it[version] = meta.version
                it[kid] = UUID.randomUUID().toString()
                it[jwtPrivateKey] = meta.jwt.privateKey
                it[jwtPublicKey] = meta.jwt.publicKey
            }
        } else {
            Meta.update({ id eq 1 }) {
                it[version] = meta.version
                it[kid] = UUID.randomUUID().toString()
                it[jwtPrivateKey] = meta.jwt.privateKey
                it[jwtPublicKey] = meta.jwt.publicKey
            }
        }
    }

    override suspend fun get(): dev.usbharu.hideout.domain.model.hideout.entity.Meta? {
        return Meta.select { id eq 1 }.singleOrNull()?.let {
            dev.usbharu.hideout.domain.model.hideout.entity.Meta(
                it[Meta.version],
                Jwt(UUID.fromString(it[kid]), it[Meta.jwtPrivateKey], it[Meta.jwtPublicKey])
            )
        }
    }
}

object Meta : Table("meta_info") {
    val id = long("id")
    val version = varchar("version", 1000)
    val kid = varchar("kid", 1000)
    val jwtPrivateKey = varchar("jwt_private_key", 100000)
    val jwtPublicKey = varchar("jwt_public_key", 100000)
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
