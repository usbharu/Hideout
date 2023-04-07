package dev.usbharu.hideout.routing.activitypub

import dev.usbharu.hideout.plugins.configureRouting
import dev.usbharu.hideout.service.activitypub.ActivityPubService
import dev.usbharu.hideout.service.activitypub.ActivityType
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

                override fun processActivity(json: String, type: ActivityType) {
                    TODO("Not yet implemented")
                }
            })
        }
        client.get("/users/test"){
            accept(ContentType.Application.Activity)
        }.let {
            assertEquals(HttpStatusCode.NotImplemented, it.status)
        }
    }
}
