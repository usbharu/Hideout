@file:OptIn(ExperimentalCoroutinesApi::class) @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package dev.usbharu.hideout.activitypub.service.objects.note

import dev.usbharu.hideout.activitypub.domain.exception.FailedToGetActivityPubResourceException
import dev.usbharu.hideout.activitypub.domain.model.Image
import dev.usbharu.hideout.activitypub.domain.model.Key
import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.domain.model.Person
import dev.usbharu.hideout.activitypub.query.NoteQueryService
import dev.usbharu.hideout.activitypub.service.common.APResourceResolveService
import dev.usbharu.hideout.activitypub.service.objects.note.APNoteServiceImpl.Companion.public
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.config.CharacterLimit
import dev.usbharu.hideout.application.service.id.TwitterSnowflakeIdGenerateService
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.query.ActorQueryService
import dev.usbharu.hideout.core.query.PostQueryService
import dev.usbharu.hideout.core.service.post.PostService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.util.date.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import utils.PostBuilder
import utils.UserBuilder
import java.time.Instant


class APNoteServiceImplTest {

    val postBuilder = Post.PostBuilder(CharacterLimit())

    @Test
    fun `fetchNote(String,String) ノートが既に存在する場合はDBから取得したものを返す`() = runTest {
        val url = "https://example.com/note"
        val post = PostBuilder.of()

        val user = UserBuilder.localUserOf(id = post.actorId)
        val actorQueryService = mock<ActorQueryService> {
            onBlocking { findById(eq(post.actorId)) } doReturn user
        }
        val expected = Note(
            id = post.apId,
            attributedTo = user.url,
            content = post.text,
            published = Instant.ofEpochMilli(post.createdAt).toString(),
            to = listOfNotNull(public, user.followers),
            sensitive = post.sensitive,
            cc = listOfNotNull(public, user.followers),
            inReplyTo = null
        )
        val noteQueryService = mock<NoteQueryService> {
            onBlocking { findByApid(eq(url)) } doReturn (expected to post)
        }
        val apNoteServiceImpl = APNoteServiceImpl(
            postRepository = mock(),
            apUserService = mock(),
            postQueryService = mock(),
            postService = mock(),
            apResourceResolveService = mock(),
            postBuilder = Post.PostBuilder(CharacterLimit()),
            noteQueryService = noteQueryService,
            mock()
        )

        val actual = apNoteServiceImpl.fetchNote(url)

        assertEquals(expected, actual)
    }

    @Test
    fun `fetchNote(String,String) ノートがDBに存在しない場合リモートに取得しにいく`() = runTest {
        val url = "https://example.com/note"
        val post = PostBuilder.of()

        val postQueryService = mock<PostQueryService> {
            onBlocking { findByApId(eq(post.apId)) } doReturn post
        }
        val user = UserBuilder.localUserOf(id = post.actorId)
        val actorQueryService = mock<ActorQueryService> {
            onBlocking { findById(eq(post.actorId)) } doReturn user
        }
        val note = Note(
            id = post.apId,
            attributedTo = user.url,
            content = post.text,
            published = Instant.ofEpochMilli(post.createdAt).toString(),
            to = listOfNotNull(public, user.followers),
            sensitive = post.sensitive,
            cc = listOfNotNull(public, user.followers),
            inReplyTo = null
        )
        val apResourceResolveService = mock<APResourceResolveService> {
            onBlocking { resolve<Note>(eq(url), any(), isNull<Long>()) } doReturn note
        }
        val noteQueryService = mock<NoteQueryService> {
            onBlocking { findByApid(eq(url)) } doThrow FailedToGetResourcesException()
        }
        val person = Person(
            name = user.name,
            id = user.url,
            preferredUsername = user.name,
            summary = user.description,
            inbox = user.inbox,
            outbox = user.outbox,
            url = user.url,
            icon = Image(
                type = emptyList(),
                mediaType = "image/png",
                url = user.url + "/icon.png"
            ),
            publicKey = Key(
                id = user.keyId,
                owner = user.url,
                publicKeyPem = user.publicKey
            ),
            endpoints = mapOf("sharedInbox" to "https://example.com/inbox"),
            followers = user.followers,
            following = user.following,
            manuallyApprovesFollowers = false

            )
        val apUserService = mock<APUserService> {
            onBlocking { fetchPersonWithEntity(eq(note.attributedTo!!), isNull()) } doReturn (person to user)
        }
        val postRepository = mock<PostRepository> {
            onBlocking { generateId() } doReturn TwitterSnowflakeIdGenerateService.generateId()
        }
        val apNoteServiceImpl = APNoteServiceImpl(
            postRepository = postRepository,
            apUserService = apUserService,
            postQueryService = postQueryService,
            postService = mock(),
            apResourceResolveService = apResourceResolveService,
            postBuilder = Post.PostBuilder(CharacterLimit()),
            noteQueryService = noteQueryService,
            mock()
        )

        val actual = apNoteServiceImpl.fetchNote(url)

        assertEquals(note, actual)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun `fetchNote(String,String) ノートをリモートから取得した際にエラーが返ってきたらFailedToGetActivityPubResourceExceptionがthrowされる`() =
        runTest {
            val url = "https://example.com/note"
            val post = PostBuilder.of()

            val postQueryService = mock<PostQueryService> {
                onBlocking { findByApId(eq(post.apId)) } doReturn post
            }
            val user = UserBuilder.localUserOf(id = post.actorId)
            val actorQueryService = mock<ActorQueryService> {
                onBlocking { findById(eq(post.actorId)) } doReturn user
            }
            val note = Note(
                id = post.apId,
                attributedTo = user.url,
                content = post.text,
                published = Instant.ofEpochMilli(post.createdAt).toString(),
                to = listOfNotNull(public, user.followers),
                sensitive = post.sensitive,
                cc = listOfNotNull(public, user.followers),
                inReplyTo = null
            )
            val apResourceResolveService = mock<APResourceResolveService> {
                val responseData = HttpResponseData(
                    HttpStatusCode.BadRequest,
                    GMTDate(),
                    Headers.Empty,
                    HttpProtocolVersion.HTTP_1_1,
                    NullBody,
                    Dispatchers.IO
                )
                onBlocking { resolve<Note>(eq(url), any(), isNull<Long>()) } doThrow ClientRequestException(
                    DefaultHttpResponse(
                        HttpClientCall(
                            HttpClient(), HttpRequestData(
                                Url("http://example.com"),
                                HttpMethod.Get,
                                Headers.Empty,
                                EmptyContent,
                                Job(null),
                                Attributes()
                            ), responseData
                        ), responseData
                    ), ""
                )
            }
            val noteQueryService = mock<NoteQueryService> {
                onBlocking { findByApid(eq(url)) } doThrow FailedToGetResourcesException()
            }
            val apNoteServiceImpl = APNoteServiceImpl(
                postRepository = mock(),
                apUserService = mock(),
                postQueryService = postQueryService,
                postService = mock(),
                apResourceResolveService = apResourceResolveService,
                postBuilder = Post.PostBuilder(CharacterLimit()),
                noteQueryService = noteQueryService,
                mock()
            )

            assertThrows<FailedToGetActivityPubResourceException> { apNoteServiceImpl.fetchNote(url) }

        }

    @Test
    fun `fetchNote(Note,String) DBに無いNoteは保存される`() = runTest {
        val user = UserBuilder.localUserOf()
        val generateId = TwitterSnowflakeIdGenerateService.generateId()
        val post = PostBuilder.of(id = generateId, userId = user.id)
        val postRepository = mock<PostRepository> {
            onBlocking { generateId() } doReturn generateId
        }
        val person = Person(
            name = user.name,
            id = user.url,
            preferredUsername = user.name,
            summary = user.name,
            inbox = user.inbox,
            outbox = user.outbox,
            url = user.url,
            icon = Image(
                mediaType = "image/png",
                url = user.url + "/icon.png"
            ),
            publicKey = Key(
                id = user.keyId,
                owner = user.url,
                publicKeyPem = user.publicKey
            ),
            endpoints = mapOf("sharedInbox" to "https://example.com/inbox"),
            following = user.following,
            followers = user.followers
        )
        val apUserService = mock<APUserService> {
            onBlocking { fetchPersonWithEntity(eq(user.url), anyOrNull()) } doReturn (person to user)
        }
        val postService = mock<PostService>()
        val noteQueryService = mock<NoteQueryService> {
            onBlocking { findByApid(eq(post.apId)) } doThrow FailedToGetResourcesException()
        }
        val apNoteServiceImpl = APNoteServiceImpl(
            postRepository = postRepository,
            apUserService = apUserService,
            postQueryService = mock(),
            postService = postService,
            apResourceResolveService = mock(),
            postBuilder = postBuilder,
            noteQueryService = noteQueryService,
            mock()
        )

        val note = Note(
            id = post.apId,
            attributedTo = user.url,
            content = post.text,
            published = Instant.ofEpochMilli(post.createdAt).toString(),
            to = listOfNotNull(public, user.followers),
            sensitive = post.sensitive,
            cc = listOfNotNull(public, user.followers),
            inReplyTo = null
        )


        val fetchNote = apNoteServiceImpl.fetchNote(note, null)
        verify(postService, times(1)).createRemote(
            eq(
                PostBuilder.of(
                    id = generateId, userId = user.id, createdAt = post.createdAt
                )
            )
        )
        assertEquals(note, fetchNote)
    }

    @Test
    fun `fetchNote DBに存在する場合取得して返す`() = runTest {

        val user = UserBuilder.localUserOf()
        val post = PostBuilder.of(userId = user.id)

        val actorQueryService = mock<ActorQueryService> {
            onBlocking { findById(eq(user.id)) } doReturn user
        }
        val note = Note(
            id = post.apId,
            attributedTo = user.url,
            content = post.text,
            published = Instant.ofEpochMilli(post.createdAt).toString(),
            to = listOfNotNull(public, user.followers),
            sensitive = post.sensitive,
            cc = listOfNotNull(public, user.followers),
            inReplyTo = null
        )
        val noteQueryService = mock<NoteQueryService> {
            onBlocking { findByApid(eq(post.apId)) } doReturn (note to post)
        }
        val apNoteServiceImpl = APNoteServiceImpl(
            postRepository = mock(),
            apUserService = mock(),
            postQueryService = mock(),
            postService = mock(),
            apResourceResolveService = mock(),
            postBuilder = postBuilder,
            noteQueryService = noteQueryService,
            mock()
        )


        val fetchNote = apNoteServiceImpl.fetchNote(note, null)
        assertEquals(note, fetchNote)
    }


}
