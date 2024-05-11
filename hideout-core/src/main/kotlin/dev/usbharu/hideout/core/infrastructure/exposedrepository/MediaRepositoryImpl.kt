/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.domain.model.media.MediaRepository
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Media.mimeType
import dev.usbharu.hideout.core.service.media.FileType
import dev.usbharu.hideout.core.service.media.MimeType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import dev.usbharu.hideout.core.domain.model.media.Media as EntityMedia

@Repository
class MediaRepositoryImpl(private val idGenerateService: IdGenerateService) : MediaRepository, AbstractRepository() {
    override val logger: Logger
        get() = Companion.logger

    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(media: EntityMedia): EntityMedia = query {
        if (Media.selectAll().where { Media.id eq media.id }.forUpdate().singleOrNull() != null
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
        return@query media
    }

    override suspend fun findById(id: Long): EntityMedia? = query {
        return@query Media
            .selectAll().where { Media.id eq id }
            .singleOrNull()
            ?.toMedia()
    }

    override suspend fun delete(id: Long): Unit = query {
        Media.deleteWhere {
            Media.id eq id
        }
    }

    override suspend fun findByRemoteUrl(remoteUrl: String): dev.usbharu.hideout.core.domain.model.media.Media? =
        query {
            return@query Media.selectAll().where { Media.remoteUrl eq remoteUrl }.singleOrNull()?.toMedia()
        }

    companion object {
        private val logger = LoggerFactory.getLogger(MediaRepositoryImpl::class.java)
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
