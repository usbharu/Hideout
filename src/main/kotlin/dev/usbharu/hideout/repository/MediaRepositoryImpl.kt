package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.dto.FileType
import dev.usbharu.hideout.exception.FailedToGetResourcesException
import dev.usbharu.hideout.service.core.IdGenerateService
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository
import dev.usbharu.hideout.domain.model.hideout.entity.Media as EntityMedia

@Repository
class MediaRepositoryImpl(private val idGenerateService: IdGenerateService) : MediaRepository {
    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(media: EntityMedia): EntityMedia {
        if (Media.select {
                Media.id eq media.id
            }.singleOrNull() != null
        ) {
            Media.update({ Media.id eq media.id }) {
                it[Media.name] = media.name
                it[Media.url] = media.url
                it[Media.remoteUrl] = media.remoteUrl
                it[Media.thumbnailUrl] = media.thumbnailUrl
                it[Media.type] = media.type.ordinal
                it[Media.blurhash] = media.blurHash
            }
        } else {
            Media.insert {
                it[Media.id] = media.id
                it[Media.name] = media.name
                it[Media.url] = media.url
                it[Media.remoteUrl] = media.remoteUrl
                it[Media.thumbnailUrl] = media.thumbnailUrl
                it[Media.type] = media.type.ordinal
                it[Media.blurhash] = media.blurHash
            }
        }
        return media
    }

    override suspend fun findById(id: Long): EntityMedia {
        return Media
            .select {
                Media.id eq id
            }
            .singleOr {
                FailedToGetResourcesException("id: $id was not found.")
            }.toMedia()
    }

    override suspend fun delete(id: Long) {
        Media.deleteWhere {
            Media.id eq id
        }
    }

    fun ResultRow.toMedia(): EntityMedia {
        return EntityMedia(
            id = this[Media.id],
            name = this[Media.name],
            url = this[Media.url],
            remoteUrl = this[Media.remoteUrl],
            thumbnailUrl = this[Media.thumbnailUrl],
            type = FileType.values().first { it.ordinal == this[Media.type] },
            blurHash = this[Media.blurhash],
        )
    }
}

object Media : Table("media") {
    val id = long("id")
    val name = varchar("name", 255)
    val url = varchar("url", 255)
    val remoteUrl = varchar("remote_url", 255).nullable()
    val thumbnailUrl = varchar("thumbnail_url", 255).nullable()
    val type = integer("type")
    val blurhash = varchar("blurhash", 255).nullable()
}
