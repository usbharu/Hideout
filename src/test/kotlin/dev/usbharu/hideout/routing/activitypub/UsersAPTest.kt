package dev.usbharu.hideout.routing.activitypub

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.User
import dev.usbharu.hideout.domain.model.UserEntity
import dev.usbharu.hideout.plugins.configureRouting
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.service.activitypub.ActivityPubService
import dev.usbharu.hideout.service.activitypub.ActivityType
import dev.usbharu.hideout.service.impl.UserService
import dev.usbharu.hideout.service.signature.HttpSignatureVerifyService
import dev.usbharu.hideout.util.HttpUtil.Activity
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class UsersAPTest {

    @Test
    fun testHandleUsersName() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        application {
            configureRouting(object : HttpSignatureVerifyService {
                override fun verify(headers: Headers): Boolean {
                    return true
                }
            }, object : ActivityPubService {
                override fun parseActivity(json: String): ActivityType {
                    TODO("Not yet implemented")
                }

                override fun processActivity(json: String, type: ActivityType): ActivityPubResponse? {
                    TODO("Not yet implemented")
                }
            },UserService(object : IUserRepository {
                override suspend fun create(user: User): UserEntity {
                    TODO("Not yet implemented")
                }

                override suspend fun findById(id: Long): UserEntity? {
                    TODO("Not yet implemented")
                }

                override suspend fun findByName(name: String): UserEntity? {
                    TODO("Not yet implemented")
                }

                override suspend fun update(userEntity: UserEntity) {
                    TODO("Not yet implemented")
                }

                override suspend fun delete(id: Long) {
                    TODO("Not yet implemented")
                }

                override suspend fun findAll(): List<User> {
                    TODO("Not yet implemented")
                }

                override suspend fun findAllByLimitAndByOffset(limit: Int, offset: Long): List<UserEntity> {
                    TODO("Not yet implemented")
                }

                override suspend fun createFollower(id: Long, follower: Long) {
                    TODO("Not yet implemented")
                }

                override suspend fun deleteFollower(id: Long, follower: Long) {
                    TODO("Not yet implemented")
                }

                override suspend fun findFollowersById(id: Long): List<UserEntity> {
                    TODO("Not yet implemented")
                }
            }))
        }
        client.get("/users/test"){
            accept(ContentType.Application.Activity)
        }.let {
            assertEquals(HttpStatusCode.NotImplemented, it.status)
        }
    }
}
