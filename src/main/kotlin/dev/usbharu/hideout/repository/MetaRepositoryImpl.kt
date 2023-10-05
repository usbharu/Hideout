package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Jwt
import org.jetbrains.exposed.sql.*
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class MetaRepositoryImpl : MetaRepository {

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
    val id: Column<Long> = long("id")
    val version: Column<String> = varchar("version", 1000)
    val kid: Column<String> = varchar("kid", 1000)
    val jwtPrivateKey: Column<String> = varchar("jwt_private_key", 100000)
    val jwtPublicKey: Column<String> = varchar("jwt_public_key", 100000)
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
