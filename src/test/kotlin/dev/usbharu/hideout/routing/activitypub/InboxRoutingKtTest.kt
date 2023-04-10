package dev.usbharu.hideout.routing.activitypub

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.plugins.configureRouting
import dev.usbharu.hideout.plugins.configureSerialization
import dev.usbharu.hideout.plugins.configureStatusPages
import dev.usbharu.hideout.service.activitypub.ActivityPubService
import dev.usbharu.hideout.service.activitypub.ActivityPubUserService
import dev.usbharu.hideout.service.impl.UserService
import dev.usbharu.hideout.service.signature.HttpSignatureVerifyService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class InboxRoutingKtTest {
    @Test
    fun `sharedInboxにGETしたら501が帰ってくる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        application {
            configureSerialization()
            configureRouting(mock(), mock(), mock(), mock())
        }
        client.get("/inbox").let {
            Assertions.assertEquals(HttpStatusCode.MethodNotAllowed, it.status)
        }
    }

    @Test
    fun `sharedInboxに空のリクエストボディでPOSTしたら400が帰ってくる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val httpSignatureVerifyService = mock<HttpSignatureVerifyService>{
            on { verify(any()) } doReturn true
        }
        val activityPubService = mock<ActivityPubService>()
        val userService = mock<UserService>()
        val activityPubUserService = mock<ActivityPubUserService>()
        application {
            configureStatusPages()
            configureSerialization()
            configureRouting(httpSignatureVerifyService, activityPubService, userService, activityPubUserService)
        }
        client.post("/inbox").let {
            Assertions.assertEquals(HttpStatusCode.BadRequest, it.status)
        }
    }
}
