package dev.usbharu.hideout.routing.activitypub

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.plugins.configureRouting
import dev.usbharu.hideout.plugins.configureSerialization
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
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
}
