@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.User
import dev.usbharu.hideout.domain.model.UsersFollowers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId


class UserRepositoryTest {

    lateinit var db: Database

    @BeforeEach
    fun beforeEach() {
        db = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction(db) {
            SchemaUtils.create(Users)
            SchemaUtils.create(UsersFollowers)
        }
    }

    @AfterEach
    fun tearDown() {
        transaction(db) {

            SchemaUtils.drop(UsersFollowers)
            SchemaUtils.drop(Users)
        }
    }

    @Test
    fun `findFollowersById フォロワー一覧を取得`() = runTest {
        val userRepository = UserRepository(db)
        val user = userRepository.save(
            User(
                id = 0L,
                name = "test",
                domain = "example.com",
                screenName = "testUser",
                description = "This user is test user.",
                password = "https://example.com/inbox",
                inbox = "",
                outbox = "https://example.com/outbox",
                url = "https://example.com",
                publicKey = "",
                createdAt = Instant.now(Clock.tickMillis(ZoneId.systemDefault()))
            )
        )
        val follower = userRepository.save(
            User(
                id = 1L,
                name = "follower",
                domain = "follower.example.com",
                screenName = "followerUser",
                description = "This user is follower user.",
                password = "",
                inbox = "https://follower.example.com/inbox",
                outbox = "https://follower.example.com/outbox",
                url = "https://follower.example.com",
                publicKey = "",
                createdAt = Instant.now(Clock.tickMillis(ZoneId.systemDefault()))
            )
        )
        val follower2 = userRepository.save(
            User(
                id = 3L,
                name = "follower2",
                domain = "follower2.example.com",
                screenName = "followerUser2",
                description = "This user is follower user 2.",
                password = "",
                inbox = "https://follower2.example.com/inbox",
                outbox = "https://follower2.example.com/outbox",
                url = "https://follower2.example.com",
                publicKey = "",
                createdAt = Instant.now(Clock.tickMillis(ZoneId.systemDefault()))
            )
        )
        userRepository.createFollower(user.id, follower.id)
        userRepository.createFollower(user.id, follower2.id)
        userRepository.findFollowersById(user.id).let {
            assertIterableEquals(listOf(follower, follower2), it)
        }

    }

    @Test
    fun `createFollower フォロワー追加`() = runTest {
        val userRepository = UserRepository(db)
        val user = userRepository.save(
            User(0L,
                "test",
                "example.com",
                "testUser",
                "This user is test user.",
                "https://example.com/inbox",
                "",
                "https://example.com/outbox",
                "https://example.com",
                publicKey = "",
                createdAt = Instant.now()
            )
        )
        val follower = userRepository.save(
            User(1L,
                "follower",
                "follower.example.com",
                "followerUser",
                "This user is follower user.",
                "",
                "https://follower.example.com/inbox",
                "https://follower.example.com/outbox",
                "https://follower.example.com",
                publicKey = "",
                createdAt = Instant.now()
            )
        )
        userRepository.createFollower(user.id, follower.id)
        transaction {

            val followerIds =
                UsersFollowers.select { UsersFollowers.userId eq user.id }.map { it[UsersFollowers.followerId] }
            assertIterableEquals(listOf(follower.id), followerIds)
        }

    }
}
