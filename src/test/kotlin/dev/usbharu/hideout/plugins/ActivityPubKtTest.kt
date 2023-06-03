package dev.usbharu.hideout.plugins

import dev.usbharu.hideout.domain.model.ap.JsonLd
import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.service.user.toPem
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.logging.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.security.KeyPairGenerator
import java.time.Instant

class ActivityPubKtTest {
    @Test
    fun HttpSignTest(): Unit = runBlocking {
        val ktorKeyMap = KtorKeyMap(object : IUserRepository {
            override suspend fun save(user: User): User {
                TODO("Not yet implemented")
            }

            override suspend fun findById(id: Long): User? {
                TODO("Not yet implemented")
            }

            override suspend fun findByIds(ids: List<Long>): List<User> {
                TODO("Not yet implemented")
            }

            override suspend fun findByName(name: String): List<User> {
                TODO()
            }

            override suspend fun findByNameAndDomain(name: String, domain: String): User {
                val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
                keyPairGenerator.initialize(1024)
                val generateKeyPair = keyPairGenerator.generateKeyPair()
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
                    generateKeyPair.private.toPem(),
                    Instant.now()
                )
            }

            override suspend fun findByDomain(domain: String): List<User> {
                TODO("Not yet implemented")
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

            override suspend fun addFollowRequest(id: Long, follower: Long) {
                TODO("Not yet implemented")
            }

            override suspend fun deleteFollowRequest(id: Long, follower: Long) {
                TODO("Not yet implemented")
            }

            override suspend fun findFollowRequestsById(id: Long, follower: Long): Boolean {
                TODO("Not yet implemented")
            }

            override suspend fun nextId(): Long {
                TODO("Not yet implemented")
            }
        })

        val httpClient = HttpClient(
            MockEngine { httpRequestData ->
                respondOk()
            }
        ) {
            install(httpSignaturePlugin) {
                keyMap = ktorKeyMap
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }

        httpClient.postAp("https://localhost", "test", JsonLd(emptyList()))
    }
}
