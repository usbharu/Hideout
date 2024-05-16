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

package dev.usbharu.hideout.activitypub.service.objects.note

import dev.usbharu.hideout.activitypub.domain.exception.FailedToGetActivityPubResourceException
import dev.usbharu.hideout.activitypub.domain.model.Announce
import dev.usbharu.hideout.activitypub.domain.model.Emoji
import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.domain.model.Person
import dev.usbharu.hideout.activitypub.query.AnnounceQueryService
import dev.usbharu.hideout.activitypub.query.NoteQueryService
import dev.usbharu.hideout.activitypub.service.common.APResourceResolveService
import dev.usbharu.hideout.activitypub.service.common.resolve
import dev.usbharu.hideout.activitypub.service.objects.emoji.EmojiService
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.service.media.MediaService
import dev.usbharu.hideout.core.service.media.RemoteMedia
import dev.usbharu.hideout.core.service.post.PostService
import io.ktor.client.plugins.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

interface APNoteService {
    suspend fun fetchNote(url: String, targetActor: String? = null): Note = fetchNoteWithEntity(url, targetActor).first
    suspend fun fetchNote(note: Note, targetActor: String? = null): Note
    suspend fun fetchNoteWithEntity(url: String, targetActor: String? = null): Pair<Note, Post>

    suspend fun fetchAnnounce(url: String, signerId: Long? = null): Pair<Announce, Post>
    suspend fun fetchAnnounce(announce: Announce, signerId: Long? = null): Pair<Announce, Post>
}

@Service
@Suppress("LongParameterList")
class APNoteServiceImpl(
    private val postRepository: PostRepository,
    private val apUserService: APUserService,
    private val postService: PostService,
    private val apResourceResolveService: APResourceResolveService,
    private val postBuilder: Post.PostBuilder,
    private val noteQueryService: NoteQueryService,
    private val mediaService: MediaService,
    private val emojiService: EmojiService,
    private val announceQueryService: AnnounceQueryService

) : APNoteService {

    private val logger = LoggerFactory.getLogger(APNoteServiceImpl::class.java)

    override suspend fun fetchNoteWithEntity(url: String, targetActor: String?): Pair<Note, Post> {
        logger.debug("START Fetch Note url: {}", url)

        val post = noteQueryService.findByApid(url)

        if (post != null) {
            logger.debug("SUCCESS Found in local url: {}", url)
            return post
        }

        logger.info("AP GET url: {}", url)
        val note = try {
            apResourceResolveService.resolve<Note>(url, null as Long?)
        } catch (e: ClientRequestException) {
            logger.warn(
                "FAILED Failed to retrieve ActivityPub resource. HTTP Status Code: {} url: {}",
                e.response.status,
                url
            )
            throw FailedToGetActivityPubResourceException("Could not retrieve $url.", e)
        }
        val savedNote = saveIfMissing(note, targetActor)
        logger.debug("SUCCESS Fetch Note url: {}", url)
        return savedNote
    }

    override suspend fun fetchAnnounce(url: String, signerId: Long?): Pair<Announce, Post> {
        logger.debug("START Fetch Announce url: {}", url)

        val post: Pair<Announce, Post>? = announceQueryService.findByApId(url)

        if (post != null) {
            logger.debug("SUCCESS Found in local url: {}", url)
            return post
        }

        logger.info("AP GET url: {}", url)

        val announce = try {
            apResourceResolveService.resolve<Announce>(url, signerId)
        } catch (e: ClientRequestException) {
            logger.warn(
                "FAILED Failed to retrieve ActivityPub resource. HTTP Status Code: {} url: {}",
                e.response.status,
                url
            )
            throw FailedToGetActivityPubResourceException("Could not retrieve $url.", e)
        }

        return fetchAnnounce(announce, signerId)
    }

    override suspend fun fetchAnnounce(announce: Announce, signerId: Long?): Pair<Announce, Post> {
        val findByApId = announceQueryService.findByApId(announce.id)

        if (findByApId != null) {
            return findByApId
        }

        val (_, actor) = apUserService.fetchPersonWithEntity(announce.actor, null)

        val (_, post) = fetchNoteWithEntity(announce.apObject, null)

        val visibility = if (announce.to.contains(public)) {
            Visibility.PUBLIC
        } else if (announce.to.contains(actor.followers) && announce.cc.contains(public)) {
            Visibility.UNLISTED
        } else if (announce.to.contains(actor.followers)) {
            Visibility.FOLLOWERS
        } else {
            Visibility.DIRECT
        }

        val createRemote = postService.createRemote(
            postBuilder.pureRepostOf(
                id = postRepository.generateId(),
                actorId = actor.id,
                visibility = visibility,
                createdAt = Instant.parse(announce.published),
                url = announce.id,
                repost = post,
                apId = announce.id
            )
        )
        return announce to createRemote
    }

    private suspend fun saveIfMissing(
        note: Note,
        targetActor: String?
    ): Pair<Note, Post> = noteQueryService.findByApid(note.id) ?: saveNote(note, targetActor)

    private suspend fun saveNote(note: Note, targetActor: String?): Pair<Note, Post> {
        val person = apUserService.fetchPersonWithEntity(
            note.attributedTo,
            targetActor
        )

        val post = postRepository.findByApId(note.id)
        if (post != null) {
            return note to post
        }

        logger.debug("VISIBILITY url: {} to: {} cc: {}", note.id, note.to, note.cc)
        val visibility = visibility(note, person)
        logger.debug("VISIBILITY is {} url: {}", visibility.name, note.id)

        val reply = note.inReplyTo?.let {
            fetchNote(it, targetActor)
            postRepository.findByUrl(it)
        }

        val quote = (note.misskeyQuote ?: note.quoteUri ?: note.quoteUrl)?.let {
            fetchNote(it, targetActor)
            postRepository.findByUrl(it)
        }

        val emojis = buildEmojis(note)

        val mediaList = note.attachment.map {
            mediaService.uploadRemoteMedia(
                RemoteMedia(
                    it.name,
                    it.url,
                    it.mediaType,
                    description = it.name
                )
            )
        }.map { it.id }

        val createPost =
            post(quote, person, note, visibility, reply, mediaList, emojis)

        val createRemote = postService.createRemote(createPost)
        return note to createRemote
    }

    private suspend fun post(
        quote: Post?,
        person: Pair<Person, Actor>,
        note: Note,
        visibility: Visibility,
        reply: Post?,
        mediaList: List<Long>,
        emojis: List<Long>
    ) = if (quote != null) {
        postBuilder.quoteRepostOf(
            id = postRepository.generateId(),
            actorId = person.second.id,
            content = note.content,
            createdAt = Instant.parse(note.published),
            visibility = visibility,
            url = note.id,
            replyId = reply?.id,
            sensitive = note.sensitive,
            apId = note.id,
            mediaIds = mediaList,
            emojiIds = emojis,
            repost = quote
        )
    } else {
        postBuilder.of(
            id = postRepository.generateId(),
            actorId = person.second.id,
            content = note.content,
            createdAt = Instant.parse(note.published).toEpochMilli(),
            visibility = visibility,
            url = note.id,
            replyId = reply?.id,
            sensitive = note.sensitive,
            apId = note.id,
            mediaIds = mediaList,
            emojiIds = emojis
        )
    }

    private suspend fun buildEmojis(note: Note) = note.tag
        .filterIsInstance<Emoji>()
        .map {
            emojiService.fetchEmoji(it).second
        }
        .map {
            it.id
        }

    private fun visibility(
        note: Note,
        person: Pair<Person, Actor>
    ): Visibility {
        val visibility = if (note.to.contains(public)) {
            Visibility.PUBLIC
        } else if (note.to.contains(person.second.followers) && note.cc.contains(public)) {
            Visibility.UNLISTED
        } else if (note.to.contains(person.second.followers)) {
            Visibility.FOLLOWERS
        } else {
            Visibility.DIRECT
        }
        return visibility
    }

    override suspend fun fetchNote(note: Note, targetActor: String?): Note =
        saveIfMissing(note, targetActor).first

    companion object {
        const val public: String = "https://www.w3.org/ns/activitystreams#Public"
    }
}
