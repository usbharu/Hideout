package dev.usbharu.hideout.routing.activitypub

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.ap.Image
import dev.usbharu.hideout.ap.Key
import dev.usbharu.hideout.ap.Person
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.User
import dev.usbharu.hideout.domain.model.UserEntity
import dev.usbharu.hideout.domain.model.job.HideoutJob
import dev.usbharu.hideout.plugins.configureRouting
import dev.usbharu.hideout.plugins.configureSerialization
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.service.activitypub.ActivityPubService
import dev.usbharu.hideout.service.activitypub.ActivityPubUserService
import dev.usbharu.hideout.service.activitypub.ActivityType
import dev.usbharu.hideout.service.impl.UserService
import dev.usbharu.hideout.service.signature.HttpSignatureVerifyService
import dev.usbharu.hideout.util.HttpUtil.Activity
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kjob.core.dsl.JobContextWithProps
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class UsersAPTest {

    @Test
    fun testHandleUsersName() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val person = Person(
            type = emptyList(),
            name = "test",
            id = "http://example.com/users/test",
            preferredUsername = "test",
            summary = "test user",
            inbox = "http://example.com/users/test/inbox",
            outbox = "http://example.com/users/test/outbox",
            url = "http://example.com/users/test",
            icon = Image(
                type = emptyList(),
                name = "http://example.com/users/test/icon.png",
                mediaType = "image/png",
                url = "http://example.com/users/test/icon.png"
            ),
            publicKey = Key(
                type = emptyList(),
                name = "Public Key",
                id = "http://example.com/users/test#pubkey",
                owner = "https://example.com/users/test",
                publicKeyPem = "-----BEGIN PUBLIC KEY-----\n\n-----END PUBLIC KEY-----"
            )
        )
        person.context = listOf("https://www.w3.org/ns/activitystreams")
        application {
            configureSerialization()
            configureRouting(object : HttpSignatureVerifyService {
                override fun verify(headers: Headers): Boolean {
                    return true
                }
            }, object : ActivityPubService {
                override fun parseActivity(json: String): ActivityType {
                    TODO("Not yet implemented")
                }

                override suspend fun processActivity(json: String, type: ActivityType): ActivityPubResponse? {
                    TODO("Not yet implemented")
                }

                override suspend fun <T : HideoutJob> processActivity(
                    job: JobContextWithProps<T>,
                    hideoutJob: HideoutJob
                ) {
                    TODO("Not yet implemented")
                }

            }, UserService(object : IUserRepository {
                override suspend fun create(user: User): UserEntity {
                    TODO("Not yet implemented")
                }

                override suspend fun findById(id: Long): UserEntity? {
                    TODO("Not yet implemented")
                }

                override suspend fun findByName(name: String): UserEntity? {
                    TODO("Not yet implemented")
                }

                override suspend fun findByUrl(url: String): UserEntity? {
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
            }), object : ActivityPubUserService {
                override suspend fun getPersonByName(name: String): Person {
                    return person
                }

                override suspend fun fetchPerson(url: String): Person {
                    TODO("Not yet implemented")
                }

            })
        }
        client.get("/users/test") {
            accept(ContentType.Application.Activity)
        }.let {
            val objectMapper = jacksonObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                objectMapper.configOverride(List::class.java).setSetterInfo(JsonSetter.Value.forValueNulls(
                    Nulls.AS_EMPTY))
            val actual = it.bodyAsText()
            val readValue = objectMapper.readValue<Person>(actual)
            println(objectMapper.writeValueAsString(person))
            println(objectMapper.writeValueAsString(readValue))
            assertEquals(person, readValue)
        }
    }
}
