package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.activitypub.domain.exception.IllegalActivityPubObjectException
import dev.usbharu.hideout.activitypub.domain.model.Create
import dev.usbharu.hideout.activitypub.domain.model.Like
import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.interfaces.api.common.ActivityPubStringResponse
import dev.usbharu.hideout.activitypub.service.activity.create.APCreateServiceImpl
import dev.usbharu.hideout.activitypub.service.`object`.note.APNoteService
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import utils.TestTransaction

class APCreateServiceImplTest {

    @Test
    fun `receiveCreate 正常なCreateを処理できる`() = runTest {
        val create = Create(
            name = "Create",
            `object` = Note(
                name = "Note",
                id = "https://example.com/note",
                attributedTo = "https://example.com/actor",
                content = "Hello World",
                published = "Date: Wed, 21 Oct 2015 07:28:00 GMT"
            ),
            actor = "https://example.com/actor",
            id = "https://example.com/create",
        )

        val apNoteService = mock<APNoteService>()
        val apCreateServiceImpl = APCreateServiceImpl(apNoteService, TestTransaction)

        val actual = ActivityPubStringResponse(HttpStatusCode.OK, "Created")

        val receiveCreate = apCreateServiceImpl.receiveCreate(create)
        verify(apNoteService, times(1)).fetchNote(any<Note>(), anyOrNull())
        assertEquals(actual, receiveCreate)
    }

    @Test
    fun `reveiveCreate CreateのobjectのtypeがNote以外の場合IllegalActivityPubObjectExceptionがthrowされる`() = runTest {
        val create = Create(
            name = "Create",
            `object` = Like(
                name = "Like",
                id = "https://example.com/note",
                actor = "https://example.com/actor",
                `object` = "https://example.com/create",
                content = "aaa"
            ),
            actor = "https://example.com/actor",
            id = "https://example.com/create",
        )

        val apCreateServiceImpl = APCreateServiceImpl(mock(), TestTransaction)
        assertThrows<IllegalActivityPubObjectException> {
            apCreateServiceImpl.receiveCreate(create)
        }
    }
}
