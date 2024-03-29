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

class PersonSerializeTest {
    @Test
    fun MastodonのPersonのデシリアライズができる() {
        val personString = """
            {
              "@context": [
                "https://www.w3.org/ns/activitystreams",
                "https://w3id.org/security/v1"
              ],
              "id": "https://mastodon.social/users/Gargron",
              "type": "Person",
              "following": "https://mastodon.social/users/Gargron/following",
              "followers": "https://mastodon.social/users/Gargron/followers",
              "inbox": "https://mastodon.social/users/Gargron/inbox",
              "outbox": "https://mastodon.social/users/Gargron/outbox",
              "featured": "https://mastodon.social/users/Gargron/collections/featured",
              "featuredTags": "https://mastodon.social/users/Gargron/collections/tags",
              "preferredUsername": "Gargron",
              "name": "Eugen Rochko",
              "summary": "\u003cp\u003eFounder, CEO and lead developer \u003cspan class=\"h-card\"\u003e\u003ca href=\"https://mastodon.social/@Mastodon\" class=\"u-url mention\"\u003e@\u003cspan\u003eMastodon\u003c/span\u003e\u003c/a\u003e\u003c/span\u003e, Germany.\u003c/p\u003e",
              "url": "https://mastodon.social/@Gargron",
              "manuallyApprovesFollowers": false,
              "discoverable": true,
              "published": "2016-03-16T00:00:00Z",
              "devices": "https://mastodon.social/users/Gargron/collections/devices",
              "alsoKnownAs": [
                "https://tooting.ai/users/Gargron"
              ],
              "publicKey": {
                "id": "https://mastodon.social/users/Gargron#main-key",
                "owner": "https://mastodon.social/users/Gargron",
                "publicKeyPem": "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvXc4vkECU2/CeuSo1wtn\nFoim94Ne1jBMYxTZ9wm2YTdJq1oiZKif06I2fOqDzY/4q/S9uccrE9Bkajv1dnkO\nVm31QjWlhVpSKynVxEWjVBO5Ienue8gND0xvHIuXf87o61poqjEoepvsQFElA5ym\novljWGSA/jpj7ozygUZhCXtaS2W5AD5tnBQUpcO0lhItYPYTjnmzcc4y2NbJV8hz\n2s2G8qKv8fyimE23gY1XrPJg+cRF+g4PqFXujjlJ7MihD9oqtLGxbu7o1cifTn3x\nBfIdPythWu5b4cujNsB3m3awJjVmx+MHQ9SugkSIYXV0Ina77cTNS0M2PYiH1PFR\nTwIDAQAB\n-----END PUBLIC KEY-----\n"
              },
              "tag": [],
              "attachment": [
                {
                  "type": "PropertyValue",
                  "name": "Patreon",
                  "value": "\u003ca href=\"https://www.patreon.com/mastodon\" target=\"_blank\" rel=\"nofollow noopener noreferrer me\"\u003e\u003cspan class=\"invisible\"\u003ehttps://www.\u003c/span\u003e\u003cspan class=\"\"\u003epatreon.com/mastodon\u003c/span\u003e\u003cspan class=\"invisible\"\u003e\u003c/span\u003e\u003c/a\u003e"
                },
                {
                  "type": "PropertyValue",
                  "name": "GitHub",
                  "value": "\u003ca href=\"https://github.com/Gargron\" target=\"_blank\" rel=\"nofollow noopener noreferrer me\"\u003e\u003cspan class=\"invisible\"\u003ehttps://\u003c/span\u003e\u003cspan class=\"\"\u003egithub.com/Gargron\u003c/span\u003e\u003cspan class=\"invisible\"\u003e\u003c/span\u003e\u003c/a\u003e"
                }
              ],
              "endpoints": {
                "sharedInbox": "https://mastodon.social/inbox"
              },
              "icon": {
                "type": "Image",
                "mediaType": "image/jpeg",
                "url": "https://files.mastodon.social/accounts/avatars/000/000/001/original/dc4286ceb8fab734.jpg"
              },
              "image": {
                "type": "Image",
                "mediaType": "image/jpeg",
                "url": "https://files.mastodon.social/accounts/headers/000/000/001/original/3b91c9965d00888b.jpeg"
              }
            }

        """.trimIndent()


        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<Person>(personString)
    }

    @Test
    fun MisskeyのnameがnullのPersonのデシリアライズができる() {
        //language=JSON
        val json = """{
  "@context": [
    "https://www.w3.org/ns/activitystreams",
    "https://w3id.org/security/v1",
    {
      "manuallyApprovesFollowers": "as:manuallyApprovesFollowers",
      "sensitive": "as:sensitive",
      "Hashtag": "as:Hashtag",
      "quoteUrl": "as:quoteUrl",
      "toot": "http://joinmastodon.org/ns#",
      "Emoji": "toot:Emoji",
      "featured": "toot:featured",
      "discoverable": "toot:discoverable",
      "schema": "http://schema.org#",
      "PropertyValue": "schema:PropertyValue",
      "value": "schema:value",
      "misskey": "https://misskey-hub.net/ns#",
      "_misskey_content": "misskey:_misskey_content",
      "_misskey_quote": "misskey:_misskey_quote",
      "_misskey_reaction": "misskey:_misskey_reaction",
      "_misskey_votes": "misskey:_misskey_votes",
      "_misskey_summary": "misskey:_misskey_summary",
      "isCat": "misskey:isCat",
      "vcard": "http://www.w3.org/2006/vcard/ns#"
    }
  ],
  "type": "Person",
  "id": "https://misskey.usbharu.dev/users/9ghwhv9zgg",
  "inbox": "https://misskey.usbharu.dev/users/9ghwhv9zgg/inbox",
  "outbox": "https://misskey.usbharu.dev/users/9ghwhv9zgg/outbox",
  "followers": "https://misskey.usbharu.dev/users/9ghwhv9zgg/followers",
  "following": "https://misskey.usbharu.dev/users/9ghwhv9zgg/following",
  "featured": "https://misskey.usbharu.dev/users/9ghwhv9zgg/collections/featured",
  "sharedInbox": "https://misskey.usbharu.dev/inbox",
  "endpoints": {
    "sharedInbox": "https://misskey.usbharu.dev/inbox"
  },
  "url": "https://misskey.usbharu.dev/@relay_test",
  "preferredUsername": "relay_test",
  "name": null,
  "summary": null,
  "_misskey_summary": null,
  "icon": null,
  "image": null,
  "tag": [],
  "manuallyApprovesFollowers": true,
  "discoverable": true,
  "publicKey": {
    "id": "https://misskey.usbharu.dev/users/9ghwhv9zgg#main-key",
    "type": "Key",
    "owner": "https://misskey.usbharu.dev/users/9ghwhv9zgg",
    "publicKeyPem": "-----BEGIN PUBLIC KEY-----\nMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA2n5yekTaI4ex5VDWzQfE\nJpWMURAMWl8RcXHLPyLQVQ/PrHp7qatGXmKJUnAOBcq1cwk+VCqTEqx8vJCOZsr1\nMq+D3FMcFdwgtJ0nivPJPx2457b5kfQ4LTkWajcFhj2qixa/XFq6hHei3LDaE6hJ\nGQbdj9NTVlMd7VpiFQkoU09vAPUwGxRoP9Qbc/sh7jrKYFB3iRmY/+zOc+PFpnfn\nG8V1d2v+lnkb9f7t0Z8y2ckk6TVcLPRZktF15eGClVptlgts3hwhrcyrpBs2Dn0U\n35KgIhkhZGAjzk0uyplpfKcserXuGvsjJvelZ3BtMGsuR4kGLHrmiRQp23mIoA1I\n8tfVuV0zPOyO3ruLk2fOjoeZ4XvFHGRNKo66Qx055/8G8Ug5vU8lvIGXm9sflaA9\ntR3AKDNsyxEfjAfrfgJ7cwlKSlLZmkU51jtYEqJ48ZkiIa6fMC0m4QGXdaXmhFWC\no1sGoIErRFpRHewdGlLC9S8R/cMxjex+n8maF0yh79y7aVvU+TS6pRWg5wYjY8r3\nZqAVg/PGRVGAbjVdIdcsjH5ClwAFBW16S633D3m7HJypwwVCzVOvMZqPqcQ/2o8c\nUk+xa88xQG+OPqoAaQqyV9iqsmCMgYM/AcX/BC2h7L2mE/PWoXnoCxGPxr5uvyBf\nHQakDGg4pFZcpVNrDlYo260CAwEAAQ==\n-----END PUBLIC KEY-----\n"
  },
  "isCat": false
}"""

        val objectMapper = ActivityPubConfig().objectMapper()

        objectMapper.readValue<Person>(json)
    }
}
