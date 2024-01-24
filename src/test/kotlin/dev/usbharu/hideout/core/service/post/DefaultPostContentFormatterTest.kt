package dev.usbharu.hideout.core.service.post

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DefaultPostContentFormatterTest {
    @Test
    fun pタグがpタグになる() {
        //language=HTML
        val html = """<p>hoge</p>"""

        val actual = DefaultPostContentFormatter().format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge</p>", "hoge"))
    }

    @Test
    fun hタグがpタグになる() {
        //language=HTML
        val html = """<h1>hoge</h1>"""

        val actual = DefaultPostContentFormatter().format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge</p>", "hoge"))
    }

    @Test
    fun pタグのネストは破棄される() {
        //language=HTML
        val html = """<p>hoge<p>fuga</p>piyo</p>"""

        val actual = DefaultPostContentFormatter().format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge</p><p>fuga</p><p>piyo</p>", "hoge\n\nfuga\n\npiyo"))
    }

    @Test
    fun spanタグは無視される() {
        //language=HTML
        val html = """<p><span>hoge</span></p>"""

        val actual = DefaultPostContentFormatter().format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge</p>", "hoge"))
    }

    @Test
    fun `2連続改行は段落に変換される`() {
        //language=HTML
        val html = """<p>hoge<br><br>fuga</p>"""

        val actual = DefaultPostContentFormatter().format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge</p><p>fuga</p>", "hoge\n\nfuga"))
    }

    @Test
    fun iタグは無視される() {
        //language=HTML
        val html = """<p><i>hoge</i></p>"""

        val actual = DefaultPostContentFormatter().format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge</p>", "hoge"))
    }

    @Test
    fun aタグはhrefの中身のみ引き継がれる() {
        //language=HTML
        val html = """<p><a href='https://example.com' class='u-url' target='_blank'>hoge</a></p>"""

        val actual = DefaultPostContentFormatter().format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p><a href=\"https://example.com\">hoge</a></p>", "hoge"))
    }

    @Test
    fun aタグの中のspanは無視される() {
        //language=HTML
        val html = """<p><a href='https://example.com'><span>hoge</span></a></p>"""

        val actual = DefaultPostContentFormatter().format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p><a href=\"https://example.com\">hoge</a></p>", "hoge"))
    }

    @Test
    fun brタグのコンテンツは改行になる() {
        //language=HTML
        val html = """<p>hoge<br>fuga</p>"""

        val actual = DefaultPostContentFormatter().format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge<br> fuga</p>", "hoge\nfuga"))
    }

    @Test
    fun いきなりテキストが来たらpタグで囲む() {
        //language=HTML
        val html = """hoge"""

        val actual = DefaultPostContentFormatter().format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge</p>", "hoge"))
    }

    @Test
    fun bodyタグが含まれていた場合消す() {
        //language=HTML
        val html = """</body><p>hoge</p>"""

        val actual = DefaultPostContentFormatter().format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge</p>", "hoge"))
    }
}
