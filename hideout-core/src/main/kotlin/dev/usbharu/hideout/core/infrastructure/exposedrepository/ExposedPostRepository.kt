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

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.post.*
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import dev.usbharu.hideout.core.domain.shared.repository.DomainEventPublishableRepository
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts.actorId
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts.apId
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts.content
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts.createdAt
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts.deleted
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts.hide
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts.id
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts.moveTo
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts.overview
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts.replyId
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts.repostId
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts.sensitive
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts.text
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts.url
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts.visibility
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.javatime.timestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedPostRepository(override val domainEventPublisher: DomainEventPublisher) :
    PostRepository,
    AbstractRepository(),
    DomainEventPublishableRepository<Post> {
    override suspend fun save(post: Post): Post {
        query {
            Posts.upsert {
                it[id] = post.id.id
                it[actorId] = post.actorId.id
                it[overview] = post.overview?.overview
                it[content] = post.content.content
                it[text] = post.content.text
                it[createdAt] = post.createdAt
                it[visibility] = post.visibility.name
                it[url] = post.url.toString()
                it[repostId] = post.repostId?.id
                it[replyId] = post.replyId?.id
                it[sensitive] = post.sensitive
                it[apId] = post.apId.toString()
                it[deleted] = post.deleted
                it[hide] = post.hide
                it[moveTo] = post.moveTo?.id
            }
            PostsMedia.deleteWhere {
                postId eq post.id.id
            }
            PostsEmojis.deleteWhere {
                postId eq post.id.id
            }
            PostsVisibleActors.deleteWhere {
                postId eq post.id.id
            }
            PostsMedia.batchInsert(post.mediaIds) {
                this[PostsMedia.postId] = post.id.id
                this[PostsMedia.mediaId] = it.id
            }
            PostsEmojis.batchInsert(post.emojiIds) {
                this[PostsEmojis.postId] = post.id.id
                this[PostsEmojis.emojiId] = it.emojiId
            }
            PostsVisibleActors.batchInsert(post.visibleActors) {
                this[PostsVisibleActors.postId] = post.id.id
                this[PostsVisibleActors.actorId] = it.id
            }
        }
        update(post)
        return post
    }

    override suspend fun saveAll(posts: List<Post>): List<Post> {
        query {
            Posts.batchUpsert(posts, id) {
                this[id] = it.id.id
                this[actorId] = it.actorId.id
                this[overview] = it.overview?.overview
                this[content] = it.content.content
                this[text] = it.content.text
                this[createdAt] = it.createdAt
                this[visibility] = it.visibility.name
                this[url] = it.url.toString()
                this[repostId] = it.repostId?.id
                this[replyId] = it.replyId?.id
                this[sensitive] = it.sensitive
                this[apId] = it.apId.toString()
                this[deleted] = it.deleted
                this[hide] = it.hide
                this[moveTo] = it.moveTo?.id
            }
            val mediaIds = posts.flatMap { post -> post.mediaIds.map { post.id.id to it.id } }
            val postsIds = posts.map { it.id.id }
            PostsMedia.deleteWhere {
                postId inList postsIds
            }
            PostsMedia.batchInsert(mediaIds) {
                this[PostsMedia.postId] = it.first
                this[PostsMedia.mediaId] = it.second
            }
            val emojiIds = posts.flatMap { post -> post.emojiIds.map { post.id.id to it.emojiId } }
            PostsEmojis.deleteWhere {
                postId inList postsIds
            }
            PostsEmojis.batchInsert(emojiIds) {
                this[PostsEmojis.postId] = it.first
                this[PostsEmojis.emojiId] = it.second
            }
            val visibleActors = posts.flatMap { post -> post.visibleActors.map { post.id.id to it.id } }
            PostsVisibleActors.deleteWhere {
                postId inList postsIds
            }
            PostsVisibleActors.batchInsert(visibleActors) {
                this[PostsVisibleActors.postId] = it.first
                this[PostsVisibleActors.actorId] = it.second
            }
        }
        posts.forEach {
            update(it)
        }
        return posts
    }

    override suspend fun findById(id: PostId): Post? {
        query {
            Posts.selectAll().where {
                Posts.id eq id.id
            }
        }
    }

    override suspend fun findByActorId(id: ActorId): List<Post> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(post: Post) {
        query {
            Posts.deleteWhere {
                id eq post.id.id
            }
        }
        update(post)
    }

    override val logger: Logger = Companion.logger

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedPostRepository::class.java)
    }
}

object Posts : Table("posts") {
    val id = long("id")
    val actorId = long("actor_id").references(Actors.id)
    val overview = varchar("overview", PostOverview.length).nullable()
    val content = varchar("content", PostContent.contentLength)
    val text = varchar("text", PostContent.textLength)
    val createdAt = timestamp("created_at")
    val visibility = varchar("visibility", 100)
    val url = varchar("url", 1000)
    val repostId = long("repost_id").references(id).nullable()
    val replyId = long("reply_id").references(id).nullable()
    val sensitive = bool("sensitive")
    val apId = varchar("ap_id", 1000)
    val deleted = bool("deleted")
    val hide = bool("hide")
    val moveTo = long("move_to").references(id).nullable()
}

object PostsMedia : Table("posts_media") {
    val postId = long("post_id").references(id, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val mediaId = long("media_id").references(Media.id, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(postId, mediaId)
}

object PostsEmojis : Table("posts_emojis") {
    val postId = long("post_id").references(id)
    val emojiId = long("emoji_id").references(CustomEmojis.id)
    override val primaryKey: PrimaryKey = PrimaryKey(postId, emojiId)
}

object PostsVisibleActors : Table("posts_visible_actors") {
    val postId = long("post_id").references(id)
    val actorId = long("actor_id").references(Actors.id)
}
