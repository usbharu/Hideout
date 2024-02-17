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

package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.application.config.ActivityPubConfig
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