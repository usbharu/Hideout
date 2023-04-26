package dev.usbharu.hideout.plugins

import dev.usbharu.hideout.domain.model.User
import dev.usbharu.hideout.repository.IUserRepository
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class KtorKeyMapTest {

    @Test
    fun getPrivateKey() {
        val ktorKeyMap = KtorKeyMap(object : IUserRepository {
            override suspend fun create(user: User): User {
                TODO("Not yet implemented")
            }

            override suspend fun findById(id: Long): User? {
                TODO("Not yet implemented")
            }

            override suspend fun findByIds(ids: List<Long>): List<User> {
                TODO("Not yet implemented")
            }

            override suspend fun findByName(name: String): User? {
                return User(
                    1,
                    "test",
                    "localhost",
                    "test",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    createdAt = LocalDateTime.now()
                )
            }

            override suspend fun findByNameAndDomains(names: List<Pair<String, String>>): List<User> {
                TODO("Not yet implemented")
            }

            override suspend fun findByUrl(url: String): User? {
                TODO("Not yet implemented")
            }

            override suspend fun findByUrls(urls: List<String>): List<User> {
                TODO("Not yet implemented")
            }

            override suspend fun update(userEntity: User) {
                TODO("Not yet implemented")
            }

            override suspend fun delete(id: Long) {
                TODO("Not yet implemented")
            }

            override suspend fun findAll(): List<User> {
                TODO("Not yet implemented")
            }

            override suspend fun findAllByLimitAndByOffset(limit: Int, offset: Long): List<User> {
                TODO("Not yet implemented")
            }

            override suspend fun createFollower(id: Long, follower: Long) {
                TODO("Not yet implemented")
            }

            override suspend fun deleteFollower(id: Long, follower: Long) {
                TODO("Not yet implemented")
            }

            override suspend fun findFollowersById(id: Long): List<User> {
                TODO("Not yet implemented")
            }

        })

        ktorKeyMap.getPrivateKey("test")
    }
}
