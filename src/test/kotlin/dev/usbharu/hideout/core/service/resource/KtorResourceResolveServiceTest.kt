package dev.usbharu.hideout.core.service.resource

import dev.usbharu.hideout.application.config.MediaConfig
import dev.usbharu.hideout.core.domain.exception.media.RemoteMediaFileSizeException
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.http.HttpHeaders.ContentLength
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class KtorResourceResolveServiceTest {

    @Spy
    private val httpClient: HttpClient = HttpClient(MockEngine {
        when (it.url.encodedPath) {
            "/over-size-limit" -> {
                respond(ByteArray(1000), HttpStatusCode.OK, Headers.build {
                    append(ContentLength, "1000")
                })
            }

            else -> {
                respond("Not Found", HttpStatusCode.NotFound)
            }
        }
    }) {
        expectSuccess = true
    }

    @Spy
    private val cacheManager: CacheManager = InMemoryCacheManager()

    @Spy
    private val mediaConfig: MediaConfig = MediaConfig()

    @InjectMocks
    private lateinit var ktorResourceResolveService: KtorResourceResolveService

    @Test
    fun ファイルサイズ制限を超えたときRemoteMediaFileSizeExceptionが発生する() = runTest {
        ktorResourceResolveService.sizeLimit = 100L
        assertThrows<RemoteMediaFileSizeException> {
            ktorResourceResolveService.resolve("https://example.com/over-size-limit")
        }
    }
}
