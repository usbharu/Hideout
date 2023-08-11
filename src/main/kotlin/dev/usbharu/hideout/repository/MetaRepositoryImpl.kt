package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Jwt
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Single
import java.util.*

@Single
class MetaRepositoryImpl(private val database: Database) : MetaRepository {

    init {
        transaction(database) {
            SchemaUtils.create(Meta)
            SchemaUtils.createMissingTablesAndColumns(Meta)
        }
    }

    override suspend fun save(meta: dev.usbharu.hideout.domain.model.hideout.entity.Meta) {
        if (Meta.select { Meta.id eq 1 }.empty()) {
            Meta.insert {
                it[id] = 1
                it[this.version] = meta.version
                it[kid] = UUID.randomUUID().toString()
                it[this.jwtPrivateKey] = meta.jwt.privateKey
                it[this.jwtPublicKey] = meta.jwt.publicKey
            }
        } else {
            Meta.update({ Meta.id eq 1 }) {
                it[this.version] = meta.version
                it[kid] = UUID.randomUUID().toString()
                it[this.jwtPrivateKey] = meta.jwt.privateKey
                it[this.jwtPublicKey] = meta.jwt.publicKey
            }
        }
    }

    override suspend fun get(): dev.usbharu.hideout.domain.model.hideout.entity.Meta? {
        return Meta.select { Meta.id eq 1 }.singleOrNull()?.let {
            dev.usbharu.hideout.domain.model.hideout.entity.Meta(
                it[Meta.version],
                Jwt(UUID.fromString(it[Meta.kid]), it[Meta.jwtPrivateKey], it[Meta.jwtPublicKey])
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
