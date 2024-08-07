package dev.usbharu.hideout.core.infrastructure.other

import dev.usbharu.hideout.core.config.HtmlSanitizeConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class DefaultPostContentFormatterTest {
    @InjectMocks
    lateinit var formatter: DefaultPostContentFormatter

    @Spy
    val policyFactory = HtmlSanitizeConfig().policy()

    @Test
    fun 文字だけのHTMLをPで囲む() {
        formatter.format("a")
    }

    @Test
    fun エレメントはそのまま() {
        formatter.format("<p>a</p>")
    }

    @Test
    fun コメントは無視() {
        formatter.format("<!-- aa -->")
    }

    @Test
    fun brタグを改行に() {
        formatter.format("<p>a<br></p>")
    }

    @Test
    fun brタグ2連続を段落に() {
        val format = formatter.format("<p>a<br><br>a</p>")

        println(format)
    }

    @Test
    fun aタグは許可される() {
        val format = formatter.format("<a href=\"https://example.com\">p</a>")

        println(format)
    }

    @Test
    fun pの中のaタグも許可される() {
        formatter.format("<p><a href=\"https://example.com\">a</a></p>")
    }
}