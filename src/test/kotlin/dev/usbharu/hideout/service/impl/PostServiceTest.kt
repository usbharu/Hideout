@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.usbharu.hideout.service.impl

import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility
import dev.usbharu.hideout.repository.Posts
import dev.usbharu.hideout.repository.UsersFollowers
import dev.usbharu.hideout.service.TwitterSnowflakeIdGenerateService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.time.Instant
import kotlin.test.assertContentEquals

class PostServiceTest {

    lateinit var db: Database

    @BeforeEach
    fun setUp() {
        db = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction(db) {
            SchemaUtils.create(Posts)
            connection.prepareStatement("SET REFERENTIAL_INTEGRITY FALSE", false).executeUpdate()
        }
    }

    @AfterEach
    fun tearDown() {
        transaction(db) {
            SchemaUtils.drop(Posts)
        }
    }

    @Test
    fun `findAll 公開投稿を取得できる`() = runTest {
        val postService = PostService(mock(), mock(), mock())

        suspend fun createPost(userId: Long, text: String, visibility: Visibility = Visibility.PUBLIC): Post {
            return Post(
                TwitterSnowflakeIdGenerateService.generateId(),
                userId,
                null,
                text,
                Instant.now().toEpochMilli(),
                visibility,
                "https://example.com${(userId.toString() + text).hashCode()}"
            )
        }

        val userA: Long = 1
        val userB: Long = 2

        val posts = listOf(
            createPost(userA, "hello"),
            createPost(userA, "hello1"),
            createPost(userA, "hello2"),
            createPost(userA, "hello3"),
            createPost(userA, "hello4"),
            createPost(userA, "hello5"),
            createPost(userA, "hello6"),
            createPost(userB, "good bay ", Visibility.FOLLOWERS),
            createPost(userB, "good bay1", Visibility.FOLLOWERS),
            createPost(userB, "good bay2", Visibility.FOLLOWERS),
            createPost(userB, "good bay3", Visibility.FOLLOWERS),
            createPost(userB, "good bay4", Visibility.FOLLOWERS),
            createPost(userB, "good bay5", Visibility.FOLLOWERS),
            createPost(userB, "good bay6", Visibility.FOLLOWERS),
        )

        transaction {
            Posts.batchInsert(posts) {
                this[Posts.id] = it.id
                this[Posts.userId] = it.userId
                this[Posts.overview] = it.overview
                this[Posts.text] = it.text
                this[Posts.createdAt] = it.createdAt
                this[Posts.visibility] = it.visibility.ordinal
                this[Posts.url] = it.url
                this[Posts.replyId] = it.replyId
                this[Posts.repostId] = it.repostId
                this[Posts.sensitive] = it.sensitive
                this[Posts.apId] = it.apId
            }
        }

        val expect = posts.filter { it.visibility == Visibility.PUBLIC }

        val actual = postService.findAll()
        assertContentEquals(expect, actual)
    }

    @Test
    fun `findAll フォロー限定投稿を見れる`() = runTest {
        val postService = PostService(mock(), mock(), mock())

        suspend fun createPost(userId: Long, text: String, visibility: Visibility = Visibility.PUBLIC): Post {
            return Post(
                TwitterSnowflakeIdGenerateService.generateId(),
                userId,
                null,
                text,
                Instant.now().toEpochMilli(),
                visibility,
                "https://example.com${(userId.toString() + text).hashCode()}"
            )
        }

        val userA: Long = 1
        val userB: Long = 2

        val posts = listOf(
            createPost(userA, "hello"),
            createPost(userA, "hello1"),
            createPost(userA, "hello2"),
            createPost(userA, "hello3"),
            createPost(userA, "hello4"),
            createPost(userA, "hello5"),
            createPost(userA, "hello6"),
            createPost(userB, "good bay ", Visibility.FOLLOWERS),
            createPost(userB, "good bay1", Visibility.FOLLOWERS),
            createPost(userB, "good bay2", Visibility.FOLLOWERS),
            createPost(userB, "good bay3", Visibility.FOLLOWERS),
            createPost(userB, "good bay4", Visibility.FOLLOWERS),
            createPost(userB, "good bay5", Visibility.FOLLOWERS),
            createPost(userB, "good bay6", Visibility.FOLLOWERS),
        )

        transaction(db) {
            SchemaUtils.create(UsersFollowers)
        }

        transaction {
            Posts.batchInsert(posts) {
                this[Posts.id] = it.id
                this[Posts.userId] = it.userId
                this[Posts.overview] = it.overview
                this[Posts.text] = it.text
                this[Posts.createdAt] = it.createdAt
                this[Posts.visibility] = it.visibility.ordinal
                this[Posts.url] = it.url
                this[Posts.replyId] = it.replyId
                this[Posts.repostId] = it.repostId
                this[Posts.sensitive] = it.sensitive
                this[Posts.apId] = it.apId
            }
            UsersFollowers.insert {
                it[id] = 100L
                it[userId] = userB
                it[followerId] = userA
            }
        }

        val actual = postService.findAll(userId = userA)
        assertContentEquals(posts, actual)
    }
}
