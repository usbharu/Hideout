package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.service.objects.note.APNoteServiceImpl.Companion.public
import dev.usbharu.hideout.application.config.ActivityPubConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NoteSerializeTest {
    @Test
    fun Noteのシリアライズができる() {
        val note = Note(
            id = "https://example.com",
            attributedTo = "https://example.com/actor",
            content = "Hello",
            published = "2023-05-20T10:28:17.308Z",
        )

        val objectMapper = ActivityPubConfig().objectMapper()

        val writeValueAsString = objectMapper.writeValueAsString(note)

        assertEquals(
            """{"type":"Note","id":"https://example.com","attributedTo":"https://example.com/actor","content":"Hello","published":"2023-05-20T10:28:17.308Z","sensitive":false}""",
            writeValueAsString
        )
    }

    @Test
    fun Noteのデシリアライズができる() {
        //language=JSON
        val json = """{
    "id": "https://misskey.usbharu.dev/notes/9f2i9cm88e",
    "type": "Note",
    "attributedTo": "https://misskey.usbharu.dev/users/97ws8y3rj6",
    "content": "<p><a href=\"https://calckey.jp/@trapezial\" class=\"u-url mention\">@trapezial@calckey.jp</a><span> いやそういうことじゃなくて、連合先と自インスタンスで状態が狂うことが多いのでどっちに合わせるべきかと…</span></p>",
    "_misskey_content": "@trapezial@calckey.jp いやそういうことじゃなくて、連合先と自インスタンスで状態が狂うことが多いのでどっちに合わせるべきかと…",
    "source": {
      "content": "@trapezial@calckey.jp いやそういうことじゃなくて、連合先と自インスタンスで状態が狂うことが多いのでどっちに合わせるべきかと…",
      "mediaType": "text/x.misskeymarkdown"
    },
    "published": "2023-05-22T14:26:53.600Z",
    "to": [
      "https://misskey.usbharu.dev/users/97ws8y3rj6/followers"
    ],
    "cc": [
      "https://www.w3.org/ns/activitystreams#Public",
      "https://calckey.jp/users/9bu1xzwjyb"
    ],
    "inReplyTo": "https://calckey.jp/notes/9f2i7ymf1d",
    "attachment": [],
    "sensitive": false,
    "tag": [
      {
        "type": "Mention",
        "href": "https://calckey.jp/users/9bu1xzwjyb",
        "name": "@trapezial@calckey.jp"
      }
    ]
  }"""

        val objectMapper = ActivityPubConfig().objectMapper()

        val readValue = objectMapper.readValue<Note>(json)

        val note = Note(
            id = "https://misskey.usbharu.dev/notes/9f2i9cm88e",
            type = listOf("Note"),
            attributedTo = "https://misskey.usbharu.dev/users/97ws8y3rj6",
            content = "<p><a href=\"https://calckey.jp/@trapezial\" class=\"u-url mention\">@trapezial@calckey.jp</a><span> いやそういうことじゃなくて、連合先と自インスタンスで状態が狂うことが多いのでどっちに合わせるべきかと…</span></p>",
            published = "2023-05-22T14:26:53.600Z",
            to = listOf("https://misskey.usbharu.dev/users/97ws8y3rj6/followers"),
            cc = listOf(public, "https://calckey.jp/users/9bu1xzwjyb"),
            sensitive = false,
            inReplyTo = "https://calckey.jp/notes/9f2i7ymf1d",
            attachment = emptyList()
        )
        assertEquals(note, readValue)
    }
}
