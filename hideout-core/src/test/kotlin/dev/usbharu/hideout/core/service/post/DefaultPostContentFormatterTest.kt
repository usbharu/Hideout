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

package dev.usbharu.hideout.core.service.post

import dev.usbharu.hideout.application.config.HtmlSanitizeConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DefaultPostContentFormatterTest {
    val defaultPostContentFormatter = DefaultPostContentFormatter(HtmlSanitizeConfig().policy())

    @Test
    fun pタグがpタグになる() {
        //language=HTML
        val html = """<p>hoge</p>"""

        val actual = defaultPostContentFormatter.format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge</p>", "hoge"))
    }

    @Test
    fun hタグがpタグになる() {
        //language=HTML
        val html = """<h1>hoge</h1>"""

        val actual = defaultPostContentFormatter.format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge</p>", "hoge"))
    }

    @Test
    fun pタグのネストは破棄される() {
        //language=HTML
        val html = """<p>hoge<p>fuga</p>piyo</p>"""

        val actual = defaultPostContentFormatter.format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge</p><p>fuga</p><p>piyo</p>", "hoge\n\nfuga\n\npiyo"))
    }

    @Test
    fun spanタグは無視される() {
        //language=HTML
        val html = """<p><span>hoge</span></p>"""

        val actual = defaultPostContentFormatter.format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge</p>", "hoge"))
    }

    @Test
    fun `2連続改行は段落に変換される`() {
        //language=HTML
        val html = """<p>hoge<br><br>fuga</p>"""

        val actual = defaultPostContentFormatter.format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge</p><p>fuga</p>", "hoge\n\nfuga"))
    }

    @Test
    fun iタグは無視される() {
        //language=HTML
        val html = """<p><i>hoge</i></p>"""

        val actual = defaultPostContentFormatter.format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge</p>", "hoge"))
    }

    @Test
    fun aタグはhrefの中身のみ引き継がれる() {
        //language=HTML
        val html = """<p><a href='https://example.com' class='u-url' target='_blank'>hoge</a></p>"""

        val actual = defaultPostContentFormatter.format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p><a href=\"https://example.com\">hoge</a></p>", "hoge"))
    }

    @Test
    fun aタグの中のspanは無視される() {
        //language=HTML
        val html = """<p><a href='https://example.com'><span>hoge</span></a></p>"""

        val actual = defaultPostContentFormatter.format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p><a href=\"https://example.com\">hoge</a></p>", "hoge"))
    }

    @Test
    fun brタグのコンテンツは改行になる() {
        //language=HTML
        val html = """<p>hoge<br>fuga</p>"""

        val actual = defaultPostContentFormatter.format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge<br> fuga</p>", "hoge\nfuga"))
    }

    @Test
    fun いきなりテキストが来たらpタグで囲む() {
        //language=HTML
        val html = """hoge"""

        val actual = defaultPostContentFormatter.format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge</p>", "hoge"))
    }

    @Test
    fun bodyタグが含まれていた場合消す() {
        //language=HTML
        val html = """</body><p>hoge</p>"""

        val actual = defaultPostContentFormatter.format(html)

        assertThat(actual).isEqualTo(FormattedPostContent("<p>hoge</p>", "hoge"))
    }

    @Test
    fun pタグの中のspanは無視される() {
        //language=HTML
        val html =
            """<p><span class="h-card" translate="no"><a href="https://test-hideout.usbharu.dev/users/testuser14" class="u-url mention">@<span>testuser14</span></a></span> tes</p>"""

        val actual = defaultPostContentFormatter.format(html)

        assertThat(actual).isEqualTo(
            FormattedPostContent(
                "<p><a href=\"https://test-hideout.usbharu.dev/users/testuser14\">@testuser14</a> tes</p>",
                "@testuser14 tes"
            )
        )
    }
}
