package dev.usbharu.hideout.activitypub.service.objects.note

import dev.usbharu.hideout.activitypub.domain.exception.FailedToGetActivityPubResourceException
import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.query.NoteQueryService
import dev.usbharu.hideout.activitypub.service.common.APResourceResolveService
import dev.usbharu.hideout.activitypub.service.common.resolve
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.query.PostQueryService
import dev.usbharu.hideout.core.service.media.MediaService
import dev.usbharu.hideout.core.service.media.RemoteMedia
import dev.usbharu.hideout.core.service.post.PostService
import io.ktor.client.plugins.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

interface APNoteService {
    suspend fun fetchNote(url: String, targetActor: String? = null): Note
    suspend fun fetchNote(note: Note, targetActor: String? = null): Note
}

@Service
@Suppress("LongParameterList")
class APNoteServiceImpl(
    private val postRepository: PostRepository,
    private val apUserService: APUserService,
    private val postQueryService: PostQueryService,
    private val postService: PostService,
    private val apResourceResolveService: APResourceResolveService,
    private val postBuilder: Post.PostBuilder,
    private val noteQueryService: NoteQueryService,
    private val mediaService: MediaService

) : APNoteService {

    private val logger = LoggerFactory.getLogger(APNoteServiceImpl::class.java)

    override suspend fun fetchNote(url: String, targetActor: String?): Note {
        logger.debug("START Fetch Note url: {}", url)
        try {
            val post = noteQueryService.findByApid(url)
            logger.debug("SUCCESS Found in local url: {}", url)
            return post.first
        } catch (_: FailedToGetResourcesException) {
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
        val savedNote = saveIfMissing(note, targetActor, url)
        logger.debug("SUCCESS Fetch Note url: {}", url)
        return savedNote
    }

    private suspend fun saveIfMissing(
        note: Note,
        targetActor: String?,
        url: String
    ): Note {
        requireNotNull(note.id) { "id is null" }



        return try {
            noteQueryService.findByApid(note.id).first
        } catch (e: FailedToGetResourcesException) {
            saveNote(note, targetActor, url)
        }


    }

    private suspend fun saveNote(note: Note, targetActor: String?, url: String): Note {
        val person = apUserService.fetchPersonWithEntity(
            note.attributedTo,
            targetActor
        )

        if (postRepository.existByApIdWithLock(note.id)) {
            return note
        }

        logger.debug("VISIBILITY url: {} to: {} cc: {}", note.id, note.to, note.cc)

        val visibility =
            if (note.to.contains(public)) {
                Visibility.PUBLIC
            } else if (note.to.contains(person.second.followers) && note.cc.contains(public)) {
                Visibility.UNLISTED
            } else if (note.to.contains(person.second.followers)) {
                Visibility.FOLLOWERS
            } else {
                Visibility.DIRECT
            }

        logger.debug("VISIBILITY is {} url: {}", visibility.name, note.id)

        val reply = note.inReplyTo?.let {
            fetchNote(it, targetActor)
            postQueryService.findByUrl(it)
        }

        val mediaList = note.attachment
            .filter { it.url != null }
            .map {
                mediaService.uploadRemoteMedia(
                    RemoteMedia(
                        it.name,
                        it.url,
                        it.mediaType,
                        description = it.name
                    )
                )
            }
            .map { it.id }

        // TODO: リモートのメディア処理を追加
        postService.createRemote(
            postBuilder.of(
                id = postRepository.generateId(),
                actorId = person.second.id,
                text = note.content,
                createdAt = Instant.parse(note.published).toEpochMilli(),
                visibility = visibility,
                url = note.id,
                replyId = reply?.id,
                sensitive = note.sensitive,
                apId = note.id,
                mediaIds = mediaList
            )
        )
        return note
    }

    override suspend fun fetchNote(note: Note, targetActor: String?): Note =
        saveIfMissing(note, targetActor, note.id)

    companion object {
        const val public: String = "https://www.w3.org/ns/activitystreams#Public"
    }
}
