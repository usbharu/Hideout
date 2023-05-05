package dev.usbharu.hideout.routing.activitypub

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ContentTypeRouteSelectorTest {
    @Test
    fun `Content-Typeが一つでマッチする`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        application {
            routing {
                route("/test") {
                    createChild(ContentTypeRouteSelector(ContentType.Application.Json)).handle {
                        call.respondText("OK")
                    }
                    get {
                        call.respondText("NG")
                    }
                }
            }
        }

        client.get("/test"){
            accept(ContentType.Text.Html)
        }.apply {
            assertEquals("NG", bodyAsText())
        }
        client.get("/test") {
            accept(ContentType.Application.Json)
        }.apply {
            assertEquals("OK", bodyAsText())
        }
    }

    @Test
    fun `Content-Typeが一つのとき違うとマッチしない`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        application {
            routing {
                route("/test") {
                    createChild(ContentTypeRouteSelector(ContentType.Application.Json)).handle {
                        call.respondText("OK")
                    }
                    get {
                        call.respondText("NG")
                    }
                }
            }
        }

        client.get("/test"){
            accept(ContentType.Text.Html)
        }.apply {
            assertEquals("NG", bodyAsText())
        }
    }

    @Test
    fun `Content-Typeがからのときマッチしない`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        application {
            routing {
                route("/test") {
                    createChild(ContentTypeRouteSelector()).handle {
                        call.respondText("OK")
                    }
                    get {
                        call.respondText("NG")
                    }
                }
            }
        }

        client.get("/test"){
            accept(ContentType.Text.Html)
        }.apply {
            assertEquals("NG", bodyAsText())
        }

        client.get("/test").apply {
            assertEquals("NG", bodyAsText())
        }
    }

    @Test
    fun `Content-Typeが複数指定されていてマッチする`() = testApplication {
        environment {
            config = ApplicationConfig("empty.conf")
        }
        application {
            routing {
                route("/test") {
                    createChild(ContentTypeRouteSelector(ContentType.Application.Json, ContentType.Text.Html)).handle {
                        call.respondText("OK")
                    }
                    get {
                        call.respondText("NG")
                    }
                }
            }
        }

        client.get("/test"){
            accept(ContentType.Text.Html)
        }.apply {
            assertEquals("OK", bodyAsText())
        }

        client.get("/test"){
            accept(ContentType.Application.Json)
        }.apply {
            assertEquals("OK", bodyAsText())
        }
        client.get("/test"){
            accept(ContentType.Application.Xml)
        }.apply {
            assertEquals("NG", bodyAsText())
        }
    }
}
