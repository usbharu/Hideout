package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.application.config.ActivityPubConfig
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class DocumentTest {
    @Test
    fun Documentをデシリアライズできる() {
        @Language("JSON") val json = """{
      "type": "Document",
      "mediaType": "image/webp",
      "url": "https://s3misskey.usbharu.dev/misskey-minio/misskey-minio/data/81ec9ad1-2581-466e-b90c-d9d2350ab95c.webp",
      "name": "ALTテスト"
    }"""

        val objectMapper = ActivityPubConfig().objectMapper()

        objectMapper.readValue<Document>(json)
    }

    @Test
    fun nameがnullなDocumentのデイシリアライズができる() {
        //language=JSON
        val json = """{
      "type": "Document",
      "mediaType": "image/webp",
      "url": "https://s3misskey.usbharu.dev/misskey-minio/misskey-minio/data/81ec9ad1-2581-466e-b90c-d9d2350ab95c.webp",
      "name": null
    }"""

        val objectMapper = ActivityPubConfig().objectMapper()

        objectMapper.readValue<Document>(json)
    }
}
