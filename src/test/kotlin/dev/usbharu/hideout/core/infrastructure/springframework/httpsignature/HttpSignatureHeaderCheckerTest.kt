package dev.usbharu.hideout.core.infrastructure.springframework.httpsignature

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.util.Base64Util
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI
import java.security.MessageDigest
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class HttpSignatureHeaderCheckerTest {

    val format = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)

    @Test
    fun `checkDate 未来はダメ`() {
        val httpSignatureHeaderChecker =
            HttpSignatureHeaderChecker(ApplicationConfig(URI.create("http://example.com").toURL()))

        val s = ZonedDateTime.now().plusDays(1).format(format)

        assertThrows<IllegalArgumentException> {
            httpSignatureHeaderChecker.checkDate(s)
        }
    }


    @Test
    fun `checkDate 過去はOK`() {
        val httpSignatureHeaderChecker =
            HttpSignatureHeaderChecker(ApplicationConfig(URI.create("http://example.com").toURL()))

        val s = ZonedDateTime.now().minusHours(1).format(format)

        assertDoesNotThrow {
            httpSignatureHeaderChecker.checkDate(s)
        }
    }

    @Test
    fun `checkDate 86400秒以上昔はダメ`() {
        val httpSignatureHeaderChecker =
            HttpSignatureHeaderChecker(ApplicationConfig(URI.create("http://example.com").toURL()))

        val s = ZonedDateTime.now().minusSeconds(86401).format(format)

        assertThrows<IllegalArgumentException> {
            httpSignatureHeaderChecker.checkDate(s)
        }
    }

    @Test
    fun `checkHost 大文字小文字の違いはセーフ`() {
        val httpSignatureHeaderChecker =
            HttpSignatureHeaderChecker(ApplicationConfig(URI.create("https://example.com").toURL()))

        assertDoesNotThrow {
            httpSignatureHeaderChecker.checkHost("example.com")
            httpSignatureHeaderChecker.checkHost("EXAMPLE.COM")
        }
    }

    @Test
    fun `checkHost サブドメインはダメ`() {
        val httpSignatureHeaderChecker =
            HttpSignatureHeaderChecker(ApplicationConfig(URI.create("https://example.com").toURL()))

        assertThrows<IllegalArgumentException> {
            httpSignatureHeaderChecker.checkHost("follower.example.com")
        }
    }

    @Test
    fun `checkDigest リクエストボディが同じなら何もしない`() {
        val httpSignatureHeaderChecker =
            HttpSignatureHeaderChecker(ApplicationConfig(URI.create("https://example.com").toURL()))


        val sha256 = MessageDigest.getInstance("SHA-256")

        @Language("JSON") val requestBody = """{"@context":"","type":"hoge"}"""

        val digest = Base64Util.encode(sha256.digest(requestBody.toByteArray()))

        assertDoesNotThrow {
            httpSignatureHeaderChecker.checkDigest(requestBody.toByteArray(), digest)
        }
    }

    @Test
    fun `checkDigest リクエストボディがちょっとでも違うとダメ`() {
        val httpSignatureHeaderChecker =
            HttpSignatureHeaderChecker(ApplicationConfig(URI.create("https://example.com").toURL()))


        val sha256 = MessageDigest.getInstance("SHA-256")

        @Language("JSON") val requestBody = """{"type":"hoge","@context":""}"""
        @Language("JSON") val requestBody2 = """{"@context":"","type":"hoge"}"""
        val digest = Base64Util.encode(sha256.digest(requestBody.toByteArray()))

        assertThrows<IllegalArgumentException> {
            httpSignatureHeaderChecker.checkDigest(requestBody2.toByteArray(), digest)
        }
    }
}