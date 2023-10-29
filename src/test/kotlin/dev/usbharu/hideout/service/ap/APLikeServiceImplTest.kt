package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.domain.model.ActivityPubStringResponse
import dev.usbharu.hideout.domain.model.ap.Like
import dev.usbharu.hideout.domain.model.ap.Note
import dev.usbharu.hideout.domain.model.ap.Person
import dev.usbharu.hideout.exception.ap.FailedToGetActivityPubResourceException
import dev.usbharu.hideout.query.PostQueryService
import dev.usbharu.hideout.service.reaction.ReactionService
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import utils.PostBuilder
import utils.TestTransaction
import utils.UserBuilder


class APLikeServiceImplTest {
    @Test
    fun `receiveLike 正常なLikeを処理できる`() = runTest {
        val actor = "https://example.com/actor"
        val note = "https://example.com/note"
        val like = Like(
            name = "Like", actor = actor, id = "htps://example.com", `object` = note, content = "aaa"
        )

        val user = UserBuilder.localUserOf()
        val apUserService = mock<APUserService> {
            onBlocking { fetchPersonWithEntity(eq(actor), anyOrNull()) } doReturn (Person(
                name = "TestUser",
                id = "https://example.com",
                preferredUsername = "Test user",
                summary = "test user",
                inbox = "https://example.com/inbox",
                outbox = "https://example.com/outbox",
                url = "https://example.com/",
                icon = null,
                publicKey = null,
                followers = null,
                following = null
            ) to user)
        }
        val apNoteService = mock<APNoteService> {
            on { fetchNoteAsync(eq(note), anyOrNull()) } doReturn async {
                Note(
                    name = "Note",
                    id = "https://example.com/note",
                    attributedTo = "https://example.com/actor",
                    content = "Hello World",
                    published = "Date: Wed, 21 Oct 2015 07:28:00 GMT",
                )
            }
        }
        val post = PostBuilder.of()
        val postQueryService = mock<PostQueryService> {
            onBlocking { findByUrl(eq(note)) } doReturn post
        }
        val reactionService = mock<ReactionService>()
        val apLikeServiceImpl = APLikeServiceImpl(
            reactionService, apUserService, apNoteService, postQueryService, TestTransaction
        )

        val actual = apLikeServiceImpl.receiveLike(like)

        verify(reactionService, times(1)).receiveReaction(eq("aaa"), eq("example.com"), eq(user.id), eq(post.id))
        assertEquals(ActivityPubStringResponse(HttpStatusCode.OK, ""), actual)
    }

    @Test
    fun `recieveLike Likeのobjectのurlが取得できないとき何もしない`() = runTest {
        val actor = "https://example.com/actor"
        val note = "https://example.com/note"
        val like = Like(
            name = "Like", actor = actor, id = "htps://example.com", `object` = note, content = "aaa"
        )

        val user = UserBuilder.localUserOf()
        val apUserService = mock<APUserService> {
            onBlocking { fetchPersonWithEntity(eq(actor), anyOrNull()) } doReturn (Person(
                name = "TestUser",
                id = "https://example.com",
                preferredUsername = "Test user",
                summary = "test user",
                inbox = "https://example.com/inbox",
                outbox = "https://example.com/outbox",
                url = "https://example.com/",
                icon = null,
                publicKey = null,
                followers = null,
                following = null
            ) to user)
        }
        val apNoteService = mock<APNoteService> {
            on { fetchNoteAsync(eq(note), anyOrNull()) } doThrow FailedToGetActivityPubResourceException()
        }

        val reactionService = mock<ReactionService>()
        val apLikeServiceImpl = APLikeServiceImpl(
            reactionService, apUserService, apNoteService, mock(), TestTransaction
        )

        val actual = apLikeServiceImpl.receiveLike(like)

        verify(reactionService, times(0)).receiveReaction(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
        assertEquals(ActivityPubStringResponse(HttpStatusCode.OK, ""), actual)
    }
}
