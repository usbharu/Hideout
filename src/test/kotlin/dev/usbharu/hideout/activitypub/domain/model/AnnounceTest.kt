package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.application.config.ActivityPubConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AnnounceTest{
    @Test
    fun mastodonのjsonをデシリアライズできる() {
        //language=JSON
        val json = """{
  "@context": "https://www.w3.org/ns/activitystreams",
  "id": "https://kb.usbharu.dev/users/usbharu/statuses/111859915842276344/activity",
  "type": "Announce",
  "actor": "https://kb.usbharu.dev/users/usbharu",
  "published": "2024-02-02T04:07:40Z",
  "to": [
    "https://kb.usbharu.dev/users/usbharu/followers"
  ],
  "cc": [
    "https://kb.usbharu.dev/users/usbharu"
  ],
  "object": "https://kb.usbharu.dev/users/usbharu/statuses/111850484548963326"
}"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<Announce>(json)


    }
}