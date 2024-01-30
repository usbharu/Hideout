package dev.usbharu.hideout.application.infrastructure.exposed

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PaginationListKtTest {
    @Test
    fun `toHttpHeader nextとprevがnullでない場合両方作成される`() {
        val paginationList = PaginationList<String, Long>(emptyList(), 1, 2)

        val httpHeader =
            paginationList.toHttpHeader({ "https://example.com?max_id=$it" }, { "https://example.com?min_id=$it" })

        assertThat(httpHeader).isEqualTo("<https://example.com?max_id=1>; rel=\"next\", <https://example.com?min_id=2>; rel=\"prev\"")
    }

    @Test
    fun `toHttpHeader nextがnullなら片方だけ作成される`() {
        val paginationList = PaginationList<String, Long>(emptyList(), 1,null)

        val httpHeader =
            paginationList.toHttpHeader({ "https://example.com?max_id=$it" }, { "https://example.com?min_id=$it" })

        assertThat(httpHeader).isEqualTo("<https://example.com?max_id=1>; rel=\"next\"")
    }

    @Test
    fun `toHttpHeader prevがnullなら片方だけ作成される`() {
        val paginationList = PaginationList<String, Long>(emptyList(), null,2)

        val httpHeader =
            paginationList.toHttpHeader({ "https://example.com?max_id=$it" }, { "https://example.com?min_id=$it" })

        assertThat(httpHeader).isEqualTo("<https://example.com?min_id=2>; rel=\"prev\"")
    }

    @Test
    fun `toHttpHeader 両方nullならnullが返ってくる`() {
        val paginationList = PaginationList<String, Long>(emptyList(), null, null)


        val httpHeader =
            paginationList.toHttpHeader({ "https://example.com?max_id=$it" }, { "https://example.com?min_id=$it" })

        assertThat(httpHeader).isNull()
    }
}