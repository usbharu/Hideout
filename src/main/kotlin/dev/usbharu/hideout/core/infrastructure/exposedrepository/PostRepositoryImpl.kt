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

import dev.usbharu.hideout.application.infrastructure.exposed.QueryMapper
import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class PostRepositoryImpl(
    private val idGenerateService: IdGenerateService,
    private val postQueryMapper: QueryMapper<Post>
) : PostRepository, AbstractRepository() {
    override val logger: Logger
        get() = Companion.logger

    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(post: Post): Post = query {
        val singleOrNull = Posts.selectAll().where { Posts.id eq post.id }.forUpdate().singleOrNull()
        if (singleOrNull == null) {
            Posts.insert {
                it[id] = post.id
                it[actorId] = post.actorId
                it[overview] = post.overview
                it[content] = post.content
                it[text] = post.text
                it[createdAt] = post.createdAt
                it[visibility] = post.visibility.ordinal
                it[url] = post.url
                it[repostId] = post.repostId
                it[replyId] = post.replyId
                it[sensitive] = post.sensitive
                it[apId] = post.apId
                it[deleted] = post.delted
            }
            PostsMedia.batchInsert(post.mediaIds) {
                this[PostsMedia.postId] = post.id
                this[PostsMedia.mediaId] = it
            }
            PostsEmojis.batchInsert(post.emojiIds) {
                this[PostsEmojis.postId] = post.id
                this[PostsEmojis.emojiId] = it
            }
        } else {
            PostsMedia.deleteWhere {
                postId eq post.id
            }
            PostsEmojis.deleteWhere {
                postId eq post.id
            }
            PostsMedia.batchInsert(post.mediaIds) {
                this[PostsMedia.postId] = post.id
                this[PostsMedia.mediaId] = it
            }
            PostsEmojis.batchInsert(post.emojiIds) {
                this[PostsEmojis.postId] = post.id
                this[PostsEmojis.emojiId] = it
            }
            Posts.update({ Posts.id eq post.id }) {
                it[actorId] = post.actorId
                it[overview] = post.overview
                it[content] = post.content
                it[text] = post.text
                it[createdAt] = post.createdAt
                it[visibility] = post.visibility.ordinal
                it[url] = post.url
                it[repostId] = post.repostId
                it[replyId] = post.replyId
                it[sensitive] = post.sensitive
                it[apId] = post.apId
                it[deleted] = post.delted
            }
        }
        return@query post
    }

    override suspend fun findById(id: Long): Post? = query {
        return@query Posts
            .leftJoin(PostsMedia)
            .leftJoin(PostsEmojis)
            .selectAll().where { Posts.id eq id }
            .let(postQueryMapper::map)
            .singleOrNull()
    }

    override suspend fun findByUrl(url: String): Post? = query {
        return@query Posts
            .leftJoin(PostsMedia)
            .leftJoin(PostsEmojis)
            .selectAll().where { Posts.url eq url }
            .let(postQueryMapper::map)
            .singleOrNull()
    }

    override suspend fun findByApId(apId: String): Post? = query {
        return@query Posts
            .leftJoin(PostsMedia)
            .leftJoin(PostsEmojis)
            .selectAll().where { Posts.apId eq apId }
            .let(postQueryMapper::map)
            .singleOrNull()
    }

    override suspend fun existByApIdWithLock(apId: String): Boolean = query {
        return@query Posts.selectAll().where { Posts.apId eq apId }.forUpdate().empty().not()
    }

    override suspend fun findByActorId(actorId: Long): List<Post> = query {
        return@query Posts
            .leftJoin(PostsMedia)
            .leftJoin(PostsEmojis)
            .selectAll().where { Posts.actorId eq actorId }.let(postQueryMapper::map)
    }

    override suspend fun countByActorId(actorId: Long): Int = query {
        return@query Posts
            .selectAll()
            .where { Posts.actorId eq actorId }
            .count()
            .toInt()
    }

    override suspend fun delete(id: Long): Unit = query {
        Posts.deleteWhere { Posts.id eq id }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PostRepositoryImpl::class.java)
    }
}

object Posts : Table() {
    val id: Column<Long> = long("id")
    val actorId: Column<Long> = long("actor_id").references(Actors.id)
    val overview: Column<String?> = varchar("overview", 100).nullable()
    val content = varchar("content", 5000)
    val text: Column<String> = varchar("text", 3000)
    val createdAt: Column<Long> = long("created_at")
    val visibility: Column<Int> = integer("visibility").default(0)
    val url: Column<String> = varchar("url", 500)
    val repostId: Column<Long?> = long("repost_id").references(id).nullable()
    val replyId: Column<Long?> = long("reply_id").references(id).nullable()
    val sensitive: Column<Boolean> = bool("sensitive").default(false)
    val apId: Column<String> = varchar("ap_id", 100).uniqueIndex()
    val deleted = bool("deleted").default(false)
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

object PostsMedia : Table("posts_media") {
    val postId = long("post_id").references(Posts.id, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val mediaId = long("media_id").references(Media.id, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(postId, mediaId)
}

object PostsEmojis : Table("posts_emojis") {
    val postId = long("post_id").references(Posts.id)
    val emojiId = long("emoji_id").references(CustomEmojis.id)
    override val primaryKey: PrimaryKey = PrimaryKey(postId, emojiId)
}
