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

@file:OptIn(ExperimentalCoroutinesApi::class) @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package dev.usbharu.hideout.activitypub.service.objects.note

import dev.usbharu.hideout.activitypub.domain.exception.FailedToGetActivityPubResourceException
import dev.usbharu.hideout.activitypub.domain.model.Image
import dev.usbharu.hideout.activitypub.domain.model.Key
import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.domain.model.Person
import dev.usbharu.hideout.activitypub.query.AnnounceQueryService
import dev.usbharu.hideout.activitypub.query.NoteQueryService
import dev.usbharu.hideout.activitypub.service.common.APResourceResolveService
import dev.usbharu.hideout.activitypub.service.objects.emoji.EmojiService
import dev.usbharu.hideout.activitypub.service.objects.note.APNoteServiceImpl.Companion.public
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.config.CharacterLimit
import dev.usbharu.hideout.application.config.HtmlSanitizeConfig
import dev.usbharu.hideout.application.service.id.TwitterSnowflakeIdGenerateService
import dev.usbharu.hideout.core.service.media.MediaService
import dev.usbharu.hideout.core.service.post.DefaultPostContentFormatter
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
import jakarta.validation.Validation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.PostBuilder
import utils.UserBuilder
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class APNoteServiceImplTest {

    @Mock
    private lateinit var postRepository: PostRepository

    @Mock
    private lateinit var apUserService: APUserService

    @Mock
    private lateinit var postService: PostService

    @Mock
    private lateinit var apResourceResolverService: APResourceResolveService

    @Spy
    private val postBuilder: Post.PostBuilder = Post.PostBuilder(
        CharacterLimit(), DefaultPostContentFormatter(HtmlSanitizeConfig().policy()),
        Validation.buildDefaultValidatorFactory().validator
    )

    @Mock
    private lateinit var noteQueryService: NoteQueryService

    @Mock
    private lateinit var mediaService: MediaService

    @Mock
    private lateinit var emojiService: EmojiService

    @Mock
    private lateinit var announceQueryService: AnnounceQueryService

    @InjectMocks
    private lateinit var apNoteServiceImpl: APNoteServiceImpl

    @Test
    fun `fetchNote(String,String) ノートが既に存在する場合はDBから取得したものを返す`() = runTest {
        val url = "https://example.com/note"
        val post = PostBuilder.of()

        val user = UserBuilder.localUserOf(id = post.actorId)
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

        whenever(noteQueryService.findByApid(eq(url))).doReturn(expected to post)

        val actual = apNoteServiceImpl.fetchNote(url)

        assertEquals(expected, actual)
    }

    @Test
    fun `fetchNote(String,String) ノートがDBに存在しない場合リモートに取得しにいく`() = runTest {
        val url = "https://example.com/note"
        val post = PostBuilder.of()


        val user = UserBuilder.localUserOf(id = post.actorId)

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

        whenever(apResourceResolverService.resolve<Note>(eq(url), any(), isNull<Long>())).doReturn(note)

        whenever(noteQueryService.findByApid(eq(url))).doReturn(null)

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

        whenever(
            apUserService.fetchPersonWithEntity(
                eq(note.attributedTo),
                isNull(),
                anyOrNull()
            )
        ).doReturn(person to user)

        whenever(postRepository.generateId()).doReturn(TwitterSnowflakeIdGenerateService.generateId())

        val actual = apNoteServiceImpl.fetchNote(url)

        assertEquals(note, actual)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun `fetchNote(String,String) ノートをリモートから取得した際にエラーが返ってきたらFailedToGetActivityPubResourceExceptionがthrowされる`() =
        runTest {
            val url = "https://example.com/note"
            val responseData = HttpResponseData(
                HttpStatusCode.BadRequest,
                GMTDate(),
                Headers.Empty,
                HttpProtocolVersion.HTTP_1_1,
                NullBody,
                Dispatchers.IO
            )
            whenever(apResourceResolverService.resolve<Note>(eq(url), any(), isNull<Long>())).doThrow(
                ClientRequestException(
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
            )

            whenever(noteQueryService.findByApid(eq(url))).doReturn(null)

            assertThrows<FailedToGetActivityPubResourceException> { apNoteServiceImpl.fetchNote(url) }

        }

    @Test
    fun `fetchNote(Note,String) DBに無いNoteは保存される`() = runTest {
        val user = UserBuilder.localUserOf()
        val generateId = TwitterSnowflakeIdGenerateService.generateId()
        val post = PostBuilder.of(id = generateId, userId = user.id)

        whenever(postRepository.generateId()).doReturn(generateId)

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

        whenever(apUserService.fetchPersonWithEntity(eq(user.url), anyOrNull(), anyOrNull())).doReturn(person to user)

        whenever(noteQueryService.findByApid(eq(post.apId))).doReturn(null)

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

        whenever(noteQueryService.findByApid(post.apId)).doReturn(note to post)

        val fetchNote = apNoteServiceImpl.fetchNote(note, null)
        assertEquals(note, fetchNote)
    }


}
