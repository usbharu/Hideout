package dev.usbharu.hideout.application.infrastructure.exposed

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PageTest {
    @Test
    fun minIdが指定されているとsinceIdは無視される() {
        val page = Page.of(1, 2, 3, 4)

        assertThat(page.maxId).isEqualTo(1)
        assertThat(page.sinceId).isNull()
        assertThat(page.minId).isEqualTo(3)
        assertThat(page.limit).isEqualTo(4)
    }

    @Test
    fun minIdがnullのときはsinceIdが使われる() {
        val page = Page.of(1, 2, null, 4)

        assertThat(page.maxId).isEqualTo(1)
        assertThat(page.minId).isNull()
        assertThat(page.sinceId).isEqualTo(2)
        assertThat(page.limit).isEqualTo(4)
    }
}