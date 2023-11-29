package dev.usbharu.hideout.core.service.media

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.toPath

class ApatcheTikaFileTypeDeterminationServiceTest {
    @Test
    fun png() {
        val apatcheTikaFileTypeDeterminationService = ApatcheTikaFileTypeDeterminationService()

        val mimeType = apatcheTikaFileTypeDeterminationService.fileType(
            String.javaClass.classLoader.getResource("400x400.png").toURI().toPath(), "400x400.png"
        )

        assertThat(mimeType.type).isEqualTo("image")
        assertThat(mimeType.subtype).isEqualTo("png")
    }
}
