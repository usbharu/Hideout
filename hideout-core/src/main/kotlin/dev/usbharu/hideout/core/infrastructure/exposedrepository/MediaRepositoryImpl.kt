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

import dev.usbharu.hideout.core.domain.model.media.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.net.URI
import dev.usbharu.hideout.core.domain.model.media.Media as EntityMedia

@Repository
class MediaRepositoryImpl : MediaRepository, AbstractRepository() {
    override val logger: Logger
        get() = Companion.logger

    override suspend fun findById(id: MediaId): dev.usbharu.hideout.core.domain.model.media.Media? {
        return query {
            return@query Media
                .selectAll().where { Media.id eq id.id }
                .singleOrNull()
                ?.toMedia()
        }
    }

    override suspend fun delete(media: dev.usbharu.hideout.core.domain.model.media.Media): Unit = query {
        Media.deleteWhere {
            id eq id
        }
    }

    override suspend fun save(media: EntityMedia): EntityMedia = query {
        if (Media.selectAll().where { Media.id eq media.id.id }.forUpdate().singleOrNull() != null
        ) {
            Media.update({ Media.id eq media.id.id }) {
                it[name] = media.name.name
                it[url] = media.url.toString()
                it[remoteUrl] = media.remoteUrl?.toString()
                it[thumbnailUrl] = media.thumbnailUrl?.toString()
                it[type] = media.type.name
                it[blurhash] = media.blurHash?.hash
                it[mimeType] = media.mimeType.type + "/" + media.mimeType.subtype
                it[description] = media.description?.description
            }
        } else {
            Media.insert {
                it[id] = media.id.id
                it[name] = media.name.name
                it[url] = media.url.toString()
                it[remoteUrl] = media.remoteUrl?.toString()
                it[thumbnailUrl] = media.thumbnailUrl?.toString()
                it[type] = media.type.name
                it[blurhash] = media.blurHash?.hash
                it[mimeType] = media.mimeType.type + "/" + media.mimeType.subtype
                it[description] = media.description?.description
            }
        }
        return@query media
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MediaRepositoryImpl::class.java)
    }
}

fun ResultRow.toMedia(): EntityMedia {
    val fileType = FileType.valueOf(this[Media.type])
    val mimeType = this[Media.mimeType]
    return EntityMedia(
        id = MediaId(this[Media.id]),
        name = MediaName(this[Media.name]),
        url = URI.create(this[Media.url]),
        remoteUrl = this[Media.remoteUrl]?.let { URI.create(it) },
        thumbnailUrl = this[Media.thumbnailUrl]?.let { URI.create(it) },
        type = fileType,
        blurHash = this[Media.blurhash]?.let { MediaBlurHash(it) },
        mimeType = MimeType(mimeType.substringBefore("/"), mimeType.substringAfter("/"), fileType),
        description = this[Media.description]?.let { MediaDescription(it) }
    )
}

object Media : Table("media") {
    val id = long("id")
    val name = varchar("name", 255)
    val url = varchar("url", 255).uniqueIndex()
    val remoteUrl = varchar("remote_url", 255).uniqueIndex().nullable()
    val thumbnailUrl = varchar("thumbnail_url", 255).uniqueIndex().nullable()
    val type = varchar("type", 100)
    val blurhash = varchar("blurhash", 255).nullable()
    val mimeType = varchar("mime_type", 255)
    val description = varchar("description", 4000).nullable()
    override val primaryKey = PrimaryKey(id)
}
