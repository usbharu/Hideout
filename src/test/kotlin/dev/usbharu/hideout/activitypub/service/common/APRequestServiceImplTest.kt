/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.activitypub.service.common

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.util.Base64Util
import dev.usbharu.httpsignature.common.HttpHeaders
import dev.usbharu.httpsignature.common.HttpMethod
import dev.usbharu.httpsignature.common.HttpRequest
import dev.usbharu.httpsignature.sign.HttpSignatureSigner
import dev.usbharu.httpsignature.sign.Signature
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.util.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import utils.JsonObjectMapper.objectMapper
import utils.UserBuilder
import java.net.URL
import java.security.MessageDigest
import java.time.format.DateTimeFormatter
import java.util.*


class APRequestServiceImplTest {
    @Test
    fun `apGet signerがnullのとき署名なしリクエストをする`() = runTest {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        val apRequestServiceImpl = APRequestServiceImpl(
            HttpClient(MockEngine {
                assertTrue(it.headers.contains("Date"))
                assertTrue(it.headers.contains("Accept"))
                assertFalse(it.headers.contains("Signature"))
                assertDoesNotThrow {
                    dateTimeFormatter.parse(it.headers["Date"])
                }
                respond("""{"type":"Follow","object": "https://example.com","actor": "https://example.com"}""")
            }),
            objectMapper,
            mock(),
            dateTimeFormatter
        )

        val responseClass = Follow(
            apObject = "https://example.com",
            actor = "https://example.com"
        )
        apRequestServiceImpl.apGet("https://example.com", responseClass = responseClass::class.java)
    }

    @Test
    fun `apGet signerがnullではないがprivateKeyがnullのとき署名なしリクエストをする`() = runTest {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        val apRequestServiceImpl = APRequestServiceImpl(
            HttpClient(MockEngine {
                assertTrue(it.headers.contains("Date"))
                assertTrue(it.headers.contains("Accept"))
                assertFalse(it.headers.contains("Signature"))
                assertDoesNotThrow {
                    dateTimeFormatter.parse(it.headers["Date"])
                }
                respond("""{"type":"Follow","object": "https://example.com","actor": "https://example.com"}""")
            }),
            objectMapper,
            mock(),
            dateTimeFormatter
        )

        val responseClass = Follow(
            apObject = "https://example.com",
            actor = "https://example.com"
        )
        apRequestServiceImpl.apGet(
            "https://example.com",
            UserBuilder.remoteUserOf(),
            responseClass = responseClass::class.java
        )
    }

    @Test
    fun `apGet signerとprivatekeyがnullではないとき署名付きリクエストをする`() = runTest {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        val httpSignatureSigner = mock<HttpSignatureSigner> {
            onBlocking {
                sign(
                    any(),
                    any(),
                    eq(listOf("(request-target)", "date", "host", "accept"))
                )
            } doReturn Signature(
                HttpRequest(URL("https://example.com"), HttpHeaders(mapOf()), HttpMethod.GET), "", ""
            )
        }
        val apRequestServiceImpl = APRequestServiceImpl(
            HttpClient(MockEngine {
                assertTrue(it.headers.contains("Date"))
                assertTrue(it.headers.contains("Accept"))
                assertTrue(it.headers.contains("Signature"))
                assertDoesNotThrow {
                    dateTimeFormatter.parse(it.headers["Date"])
                }
                respond("""{"type":"Follow","object": "https://example.com","actor": "https://example.com"}""")
            }),
            objectMapper,
            httpSignatureSigner,
            dateTimeFormatter
        )

        val responseClass = Follow(
            apObject = "https://example.com",
            actor = "https://example.com"
        )
        apRequestServiceImpl.apGet(
            "https://example.com",
            UserBuilder.localUserOf(
                privateKey = "-----BEGIN PRIVATE KEY-----\n" +
                        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDJhNETcFVoZW36\n" +
                        "pDiaaUDa1FsWGqULUa6jDWYbMXFirbbceJEfvaasac+E8VUQ3krrEhYBArntB1do\n" +
                        "1Zq/MpI97WaQefwrBmjJwjYglB8AHF1RRqFlJ0aABMBvuHiIzuTPv4dLS4+pJQWl\n" +
                        "iE9TKsxXgUrEdWLmpSukZpyiWnrgFtJ8322LXRuL9+O4ivns1JfozbrHTprI4ohe\n" +
                        "6taZJX1mhGBXQT+U/UrEILk+z70P2rrwxwerdO7s6nkkC3ieJWdi924/AopDlg12\n" +
                        "8udubLPbpWVVrHbSKviUr3VKBKGe4xmvO7hqpGwKmctaXRVPjh/ue2mCIzv3qyxQ\n" +
                        "3n2Xyhb3AgMBAAECggEAGddiSC/bg+ud0spER+i/XFBm7cq052KuFlKdiVcpxxGn\n" +
                        "pVYApiVXvjxDVDTuR5950/MZxz9mQDL0zoi1s1b00eQjhttdrta/kT/KWRslboo0\n" +
                        "nTuFbsc+jyQM2Ua6jjCZvto8qzchUPtiYfu80Floor/9qnuzFwiPNCHEbD1WDG4m\n" +
                        "fLuH+INnGY6eRF+pgly1dykGs18DaR3vC9CWOqR9PWH+p/myksVymR5adKauMc+l\n" +
                        "gjLaeB1YjnzXnHYLqwtCgh053kedPG/xZZwq48YNP5npSBIHsd9g8JIPVNOOc6+s\n" +
                        "bbFqD9aQQxG/WaA5hxHRupLkKGjE6lw4SnVYzKMZIQKBgQDryFa3qzJIBrCQQa0r\n" +
                        "6YlmZeeCQ8mQL8d0gY0Ixo9Gm2/9J71m/oBkhOqnS6Z5e5UHS5iVaqM7sIOZ2Ony\n" +
                        "kPADAtxUsk71Il+z+JgyN3OQ+DROLREi2TIWS523hbtN7e/fRFs7KoN6cH7IeF13\n" +
                        "3pphg9+WWRGX7y1zMd1puY/gSwKBgQDazFrAt/oZbnDhkX350OdIybz62OHNyuZv\n" +
                        "UX9fFl9i93SF+UhOpJ8YvDJtfLEJUkwO+V3TB+we1OlOYMTqir5M8GFn6YDotwxB\n" +
                        "r6eT886UpJgtJwswwwW2yaXo7zXaeg3ovRE8RJ4y++Mhuqeq3ajIo7xlhQjzBDEf\n" +
                        "ZAqasSWwhQKBgQC0VbUlo1XAywUOQH0/oc4KOJS6CDjJBBIsZM3G0X9SBJ7B5Dwz\n" +
                        "4yG2QAbtT6oTLldMjiA036vbgmUVLVe5w+sekniMexhy2wiRsOhPOCQ20+/Ffyil\n" +
                        "G7P4Y3tMm4cn0n1tqW2RsjF/Wz1M/OqYPPSc8uz2pEcVisSbX582Nsv5QwKBgEuy\n" +
                        "vAtFG6BE14UTIzSVFA/YzCs1choTAtqspZauVN4WoxffASdESU7zfbbnlxCUin/7\n" +
                        "wnxKl2SrYPSfAkHrMp/H4stivBjHi9QGA8JqbaR7tbKZeYOrVYTCC0alzEoERF+r\n" +
                        "WhUx4FHfV9vJikzRV53jGEE/X7NEVgJ4SDrw4wtJAoGAAMJ2kOIL3HSQPd8csXeU\n" +
                        "nkxLNzBsFpF76LVmLdzJttlr8HWBjLP/EJFQZFzuf5Hd38cLUOWWD3FRZVw0dUcN\n" +
                        "RSqfIYT4yDc/9GSRb6rOkdmBUWpTsrZjXBo0MC3p1QE6sNO8JfvmxHTSAe8apBh/\n" +
                        "gaYuQGh0lNa23HwwFoJxuoc=\n" +
                        "-----END PRIVATE KEY-----"
            ),
            responseClass = responseClass::class.java
        )
    }

    @Test
    fun `apPost bodyがnullでないときcontextにactivitystreamのURLを追加する`() = runTest {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        val apRequestServiceImpl = APRequestServiceImpl(HttpClient(MockEngine {
            val readValue = objectMapper.readValue<Follow>(it.body.toByteArray())

            assertThat(readValue.context).contains("https://www.w3.org/ns/activitystreams")

            respondOk("{}")
        }), objectMapper, mock(), dateTimeFormatter)

        val body = Follow(
            apObject = "https://example.com",
            actor = "https://example.com"
        )
        apRequestServiceImpl.apPost("https://example.com", body, null)
    }

    @Test
    fun `apPost bodyがnullのときリクエストボディは空`() = runTest {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        val apRequestServiceImpl = APRequestServiceImpl(HttpClient(MockEngine {

            assertEquals(0, it.body.toByteArray().size)

            respondOk("{}")
        }), objectMapper, mock(), dateTimeFormatter)

        apRequestServiceImpl.apPost("https://example.com", null, null)
    }

    @Test
    fun `apPost signerがnullのとき署名なしリクエストをする`() = runTest {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        val apRequestServiceImpl = APRequestServiceImpl(HttpClient(MockEngine {
            val src = it.body.toByteArray()
            val readValue = objectMapper.readValue<Follow>(src)

            assertThat(readValue.context).contains("https://www.w3.org/ns/activitystreams")

            val map = it.headers.toMap()
            assertThat(map).containsKey("Date")
                .containsKey("Digest")
                .containsKey("Accept")
                .doesNotContainKey("Signature")

            assertDoesNotThrow {
                dateTimeFormatter.parse(it.headers["Date"])
            }
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val digest = Base64Util.encode(messageDigest.digest(src))

            assertEquals(digest, it.headers["Digest"].orEmpty().split("256=").last())

            respondOk("{}")
        }), objectMapper, mock(), dateTimeFormatter)

        val body = Follow(
            apObject = "https://example.com",
            actor = "https://example.com"
        )
        apRequestServiceImpl.apPost("https://example.com", body, null)
    }

    @Test
    fun `apPost signerがnullではないがprivatekeyがnullのとき署名なしリクエストをする`() = runTest {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        val apRequestServiceImpl = APRequestServiceImpl(HttpClient(MockEngine {
            val src = it.body.toByteArray()
            val readValue = objectMapper.readValue<Follow>(src)

            assertThat(readValue.context).contains("https://www.w3.org/ns/activitystreams")

            val map = it.headers.toMap()
            assertThat(map).containsKey("Date")
                .containsKey("Digest")
                .containsKey("Accept")
                .doesNotContainKey("Signature")

            val messageDigest = MessageDigest.getInstance("SHA-256")
            val digest = Base64Util.encode(messageDigest.digest(src))

            assertEquals(digest, it.headers["Digest"].orEmpty().split("256=").last())

            respondOk("{}")
        }), objectMapper, mock(), dateTimeFormatter)

        val body = Follow(
            apObject = "https://example.com",
            actor = "https://example.com"
        )
        apRequestServiceImpl.apPost("https://example.com", body, UserBuilder.remoteUserOf())
    }

    @Test
    fun `apPost signerがnullではないとき署名付きリクエストをする`() = runTest {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        val httpSignatureSigner = mock<HttpSignatureSigner> {
            onBlocking {
                sign(
                    any(),
                    any(),
                    eq(listOf("(request-target)", "date", "host", "digest"))
                )
            } doReturn Signature(
                HttpRequest(URL("https://example.com"), HttpHeaders(mapOf()), HttpMethod.POST), "", ""
            )
        }
        val apRequestServiceImpl = APRequestServiceImpl(HttpClient(MockEngine {
            val src = it.body.toByteArray()
            val readValue = objectMapper.readValue<Follow>(src)

            assertThat(readValue.context).contains("https://www.w3.org/ns/activitystreams")

            val map = it.headers.toMap()
            assertThat(map).containsKey("Date")
                .containsKey("Digest")
                .containsKey("Accept")
                .containsKey("Signature")

            val messageDigest = MessageDigest.getInstance("SHA-256")
            val digest = Base64Util.encode(messageDigest.digest(src))

            assertEquals(digest, it.headers["Digest"].orEmpty().split("256=").last())

            respondOk("{}")
        }), objectMapper, httpSignatureSigner, dateTimeFormatter)

        val body = Follow(
            apObject = "https://example.com",
            actor = "https://example.com"
        )
        apRequestServiceImpl.apPost(
            "https://example.com", body, UserBuilder.localUserOf(
                privateKey = "-----BEGIN PRIVATE KEY-----\n" +
                        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC1+pj+/t5WwU6P\n" +
                        "OiaAKfOHCUVMdOR5e2Jp0BUYfAFpim27pLsHRXVjdzs+D4gvDnQWC0FMltPyBldk\n" +
                        "gjisNMtTKgTTsYhlLlSi+yRDZvIQyH4b7xSX0hCeflTrTkt18ZldBRPfMHE0KSho\n" +
                        "mm3Lc7ubF32YzGoo3A3qEVDAR9dVQOnt/GXLiN4RHoStX+y5UiP6B4s49nyEwuLm\n" +
                        "+HE4ph3Loqn0dTEL4cEuI8ZX51J3mTKT3rmMo0wCXXOm8gD2Fu7hYEdr9ulWF8GO\n" +
                        "yVe7Miu9prbBlY/r4skdXc5o6uE8tsPT88Ly9lSr3xqbmn1/EhyqBRdcyoj28C65\n" +
                        "cThO38jvAgMBAAECggEAFbOaXkJ3smHgI/17zOnz1EU7QehovMIFlPfPJDnZk0QC\n" +
                        "XQ/CjBXw71kvM/H3PCFdn6lc8qzD/sdZ0a8j4glzu+m1ZKd1zBcv2bXYd79Fm9HF\n" +
                        "FEC5NHfFKpmHN/6AykJzFyA9Y+7reRx1aLAN6ubU1ySAgmHSQSgo8qJ4/k0y9UQS\n" +
                        "EbjxQL5ziXuxRBMn7InLUGLl5UfCC0V1R8MZQAe+fApKDXMQ0LHSJUg1A365PyhV\n" +
                        "seotqvhurHH3UVHf5n0/sFeqp2hI4ymR3cs4kd8IuNIXE7afh+89IyuVKMvJh+iQ\n" +
                        "ZGO1RL0v0mNtUpI81agSrrQ4LRBjSkP+5s5PdXTrSQKBgQD2lwMXLylhQzhRyhLx\n" +
                        "sSPRf9mKDUcretwA5Fh9GuAurKOz7SvIdzrUPFYUTUKSTwk8mVRRamkFtJ8IOB7Z\n" +
                        "MLenlFqxs4XrNGBcZxut5cPv68xn2F00Y4HwX9xmEi+vniNVrDpdVLxEoVfm1pBk\n" +
                        "02ZHCcfYVN0t8dnvXvlL+eJSqQKBgQC87GMoMvFnWgT23wdXtQH+F+gQAMUrkMWz\n" +
                        "Ld2uRwuSVQArgp+YgnwWMlYlFp/QIW90t7UVmf6bHIplO5bL2OwayIO1r/WxD1eN\n" +
                        "RLrFIeDbtCZWQTHUypnWtl+9lrh/RrCjZo/sZFl07OSIKgGM37j9taG6Nv6fV7gv\n" +
                        "T0q6eDCV1wKBgGh3CUQlIq6lv5JGvUfO95GlTA+EGIZ/Af0Ov74gSKD9Wky7STUf\n" +
                        "7bhD52OqZ218NjmJ64KiReO45TaiL89rKCLCYrmtiCpgggIjXEKLeDqH9ox3yOSM\n" +
                        "01t2APTs926629VLpV4sq6WXhJmyhHFybX3i0tr++MSiFOWnoo1hS1QhAoGAfVY6\n" +
                        "ppW9kDqppnrqrSZ6Lu//VnacWL3QW4JnWtLpe2iHF1auuQiAeF1mx25OEk/MWNvz\n" +
                        "+GPVBWUW7/hrn8vHQDGdJ/GYB6LNC/z4CAbk3f2TnY/dFnZfP5J4zBftSQtF7vIB\n" +
                        "M+yTaL4tE6UCqEpYuYFBzX/kxyP0Hvb09eb9HLsCgYEArFSgWpaLbADcWd+ygWls\n" +
                        "LNfch1Yl2bnqXKz1Dnw3J4l2gbVNcABXQLrB6upjtkytxj4ae66Sio7nf+dB5yJ6\n" +
                        "NVY7i4C0JrniY2OvLnuz2bKpaTgMPJxyZqGQ6Vu2b3x9WhcpiI83SCuCUgBKxjh/\n" +
                        "qEGv2ZqFfnNVrz5RXLHBoG4=\n" +
                        "-----END PRIVATE KEY-----"
            )
        )
    }

    @Test
    fun `apPost responseClassを指定した場合はjsonでシリアライズされる`() = runTest {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        val apRequestServiceImpl = APRequestServiceImpl(HttpClient(MockEngine {
            val src = it.body.toByteArray()
            val readValue = objectMapper.readValue<Follow>(src)

            assertThat(readValue.context).contains("https://www.w3.org/ns/activitystreams")

            respondOk(src.decodeToString())
        }), objectMapper, mock(), dateTimeFormatter)

        val body = Follow(
            apObject = "https://example.com",
            actor = "https://example.com"
        )
        val actual = apRequestServiceImpl.apPost("https://example.com", body, null, body::class.java)

        assertThat(body).isEqualTo(actual)
    }
}
