package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.model.meta.Jwt
import dev.usbharu.hideout.core.domain.model.meta.MetaRepository
import org.jetbrains.exposed.sql.*
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class MetaRepositoryImpl : MetaRepository {

    override suspend fun save(meta: dev.usbharu.hideout.core.domain.model.meta.Meta) {
        if (Meta.selectAll().where { Meta.id eq 1 }.empty()) {
            Meta.insert {
                it[id] = 1
                it[version] = meta.version
                it[kid] = UUID.randomUUID().toString()
                it[jwtPrivateKey] = meta.jwt.privateKey
                it[jwtPublicKey] = meta.jwt.publicKey
            }
        } else {
            Meta.update({ Meta.id eq 1 }) {
                it[version] = meta.version
                it[kid] = UUID.randomUUID().toString()
                it[jwtPrivateKey] = meta.jwt.privateKey
                it[jwtPublicKey] = meta.jwt.publicKey
            }
        }
    }

    override suspend fun get(): dev.usbharu.hideout.core.domain.model.meta.Meta? {
        return Meta.selectAll().where { Meta.id eq 1 }.singleOrNull()?.let {
            dev.usbharu.hideout.core.domain.model.meta.Meta(
                it[Meta.version],
                Jwt(UUID.fromString(it[Meta.kid]), it[Meta.jwtPrivateKey], it[Meta.jwtPublicKey])
            )
        }
    }
}

object Meta : Table("meta_info") {
    val id: Column<Long> = long("id")
    val version: Column<String> = varchar("version", 1000)
    val kid: Column<String> = varchar("kid", 1000)
    val jwtPrivateKey: Column<String> = varchar("jwt_private_key", 100000)
    val jwtPublicKey: Column<String> = varchar("jwt_public_key", 100000)
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
