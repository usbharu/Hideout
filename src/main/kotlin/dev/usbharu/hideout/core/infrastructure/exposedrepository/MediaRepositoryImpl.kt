package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.media.MediaRepository
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Media.mimeType
import dev.usbharu.hideout.core.service.media.FileType
import dev.usbharu.hideout.core.service.media.MimeType
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository
import dev.usbharu.hideout.core.domain.model.media.Media as EntityMedia

@Repository
class MediaRepositoryImpl(private val idGenerateService: IdGenerateService) : MediaRepository {
    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(media: EntityMedia): EntityMedia {
        if (Media.select {
                Media.id eq media.id
            }.singleOrNull() != null
        ) {
            Media.update({ Media.id eq media.id }) {
                it[name] = media.name
                it[url] = media.url
                it[remoteUrl] = media.remoteUrl
                it[thumbnailUrl] = media.thumbnailUrl
                it[type] = media.type.ordinal
                it[blurhash] = media.blurHash
                it[mimeType] = media.mimeType.type + "/" + media.mimeType.subtype
                it[description] = media.description
            }
        } else {
            Media.insert {
                it[id] = media.id
                it[name] = media.name
                it[url] = media.url
                it[remoteUrl] = media.remoteUrl
                it[thumbnailUrl] = media.thumbnailUrl
                it[type] = media.type.ordinal
                it[blurhash] = media.blurHash
                it[mimeType] = media.mimeType.type + "/" + media.mimeType.subtype
                it[description] = media.description
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
}

fun ResultRow.toMedia(): EntityMedia {
    val fileType = FileType.values().first { it.ordinal == this[Media.type] }
    val mimeType = this[Media.mimeType]
    return EntityMedia(
        id = this[Media.id],
        name = this[Media.name],
        url = this[Media.url],
        remoteUrl = this[Media.remoteUrl],
        thumbnailUrl = this[Media.thumbnailUrl],
        type = fileType,
        blurHash = this[Media.blurhash],
        mimeType = MimeType(mimeType.substringBefore("/"), mimeType.substringAfter("/"), fileType),
        description = this[Media.description]
    )
}

fun ResultRow.toMediaOrNull(): EntityMedia? {
    val fileType = FileType.values().first { it.ordinal == (this.getOrNull(Media.type) ?: return null) }
    val mimeType = this.getOrNull(Media.mimeType) ?: return null
    return EntityMedia(
        id = this.getOrNull(Media.id) ?: return null,
        name = this.getOrNull(Media.name) ?: return null,
        url = this.getOrNull(Media.url) ?: return null,
        remoteUrl = this[Media.remoteUrl],
        thumbnailUrl = this[Media.thumbnailUrl],
        type = FileType.values().first { it.ordinal == this.getOrNull(Media.type) },
        blurHash = this[Media.blurhash],
        mimeType = MimeType(mimeType.substringBefore("/"), mimeType.substringAfter("/"), fileType),
        description = this[Media.description]
    )
}

object Media : Table("media") {
    val id = long("id")
    val name = varchar("name", 255)
    val url = varchar("url", 255).uniqueIndex()
    val remoteUrl = varchar("remote_url", 255).uniqueIndex().nullable()
    val thumbnailUrl = varchar("thumbnail_url", 255).uniqueIndex().nullable()
    val type = integer("type")
    val blurhash = varchar("blurhash", 255).nullable()
    val mimeType = varchar("mime_type", 255)
    val description = varchar("description", 4000).nullable()
    override val primaryKey = PrimaryKey(id)
}
