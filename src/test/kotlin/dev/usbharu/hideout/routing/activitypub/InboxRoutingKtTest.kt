package dev.usbharu.hideout.routing.activitypub

import dev.usbharu.hideout.exception.JsonParseException
import dev.usbharu.hideout.plugins.configureSerialization
import dev.usbharu.hideout.plugins.configureStatusPages
import dev.usbharu.hideout.service.activitypub.ActivityPubService
import dev.usbharu.hideout.service.activitypub.ActivityPubUserService
import dev.usbharu.hideout.service.impl.IUserService
import dev.usbharu.hideout.service.signature.HttpSignatureVerifyService
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock

class InboxRoutingKtTest {
    @Test
    fun `sharedInboxにGETしたら405が帰ってくる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        application {
            configureSerialization()
            routing {
                inbox(mock(), mock())
            }
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
        val httpSignatureVerifyService = mock<HttpSignatureVerifyService> {
            on { verify(any()) } doReturn true
        }
        val activityPubService = mock<ActivityPubService> {
            on { parseActivity(any()) } doThrow JsonParseException()
        }
        mock<IUserService>()
        mock<ActivityPubUserService>()
        application {
            configureStatusPages()
            configureSerialization()
            routing {
                inbox(httpSignatureVerifyService, activityPubService)
            }
        }
        client.post("/inbox").let {
            Assertions.assertEquals(HttpStatusCode.BadRequest, it.status)
        }
    }

    @Test
    fun `ユーザのinboxにGETしたら405が帰ってくる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        application {
            configureSerialization()
            routing {
                inbox(mock(), mock())
            }
        }
        client.get("/users/test/inbox").let {
            Assertions.assertEquals(HttpStatusCode.MethodNotAllowed, it.status)
        }
    }

    @Test
    fun `ユーザーのinboxに空のリクエストボディでPOSTしたら400が帰ってくる`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        val httpSignatureVerifyService = mock<HttpSignatureVerifyService> {
            on { verify(any()) } doReturn true
        }
        val activityPubService = mock<ActivityPubService> {
            on { parseActivity(any()) } doThrow JsonParseException()
        }
        mock<IUserService>()
        mock<ActivityPubUserService>()
        application {
            configureStatusPages()
            configureSerialization()
            routing {
                inbox(httpSignatureVerifyService, activityPubService)
            }
        }
        client.post("/users/test/inbox").let {
            Assertions.assertEquals(HttpStatusCode.BadRequest, it.status)
        }
    }
}
