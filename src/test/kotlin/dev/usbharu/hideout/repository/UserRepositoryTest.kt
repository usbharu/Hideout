@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.User
import dev.usbharu.hideout.domain.model.Users
import dev.usbharu.hideout.domain.model.UsersFollowers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import utils.DatabaseTestBase

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryTest : DatabaseTestBase() {

    @BeforeAll
    fun beforeAll() {
        SchemaUtils.create(Users)
        SchemaUtils.create(UsersFollowers)
    }

    @Test
    fun `findFollowersById フォロワー一覧を取得`() = runTest {
        newSuspendedTransaction {
            val userRepository = UserRepository(db)
            val user = userRepository.create(
                User(
                    "test",
                    "example.com",
                    "testUser",
                    "This user is test user.",
                    "https://example.com/inbox",
                    "https://example.com/outbox",
                    "https://example.com"
                )
            )
            val follower = userRepository.create(
                User(
                    "follower",
                    "follower.example.com",
                    "followerUser",
                    "This user is follower user.",
                    "https://follower.example.com/inbox",
                    "https://follower.example.com/outbox",
                    "https://follower.example.com"
                )
            )
            val follower2 = userRepository.create(
                User(
                    "follower2",
                    "follower2.example.com",
                    "followerUser2",
                    "This user is follower user 2.",
                    "https://follower2.example.com/inbox",
                    "https://follower2.example.com/outbox",
                    "https://follower2.example.com"
                )
            )
            userRepository.createFollower(user.id, follower.id)
            userRepository.createFollower(user.id, follower2.id)
            userRepository.findFollowersById(user.id).let {
                assertIterableEquals(listOf(follower, follower2), it)
            }
        }
    }

    @Test
    fun `createFollower フォロワー追加`() = runTest {
        newSuspendedTransaction {
            val userRepository = UserRepository(db)
            val user = userRepository.create(
                User(
                    "test",
                    "example.com",
                    "testUser",
                    "This user is test user.",
                    "https://example.com/inbox",
                    "https://example.com/outbox",
                    "https://example.com"
                )
            )
            val follower = userRepository.create(
                User(
                    "follower",
                    "follower.example.com",
                    "followerUser",
                    "This user is follower user.",
                    "https://follower.example.com/inbox",
                    "https://follower.example.com/outbox",
                    "https://follower.example.com"
                )
            )
            userRepository.createFollower(user.id, follower.id)
            val followerIds = UsersFollowers.select { UsersFollowers.userId eq user.id }.map { it[UsersFollowers.followerId] }
            assertIterableEquals(listOf(follower.id), followerIds)
        }
    }
}
