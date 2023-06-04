package dev.usbharu.hideout.service.activitypub

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.ap.Create
import dev.usbharu.hideout.domain.model.ap.Note
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility
import dev.usbharu.hideout.domain.model.job.DeliverPostJob
import dev.usbharu.hideout.exception.ap.IllegalActivityPubObjectException
import dev.usbharu.hideout.plugins.getAp
import dev.usbharu.hideout.plugins.postAp
import dev.usbharu.hideout.repository.IPostRepository
import dev.usbharu.hideout.service.job.JobQueueParentService
import dev.usbharu.hideout.service.user.IUserService
import io.ktor.client.*
import io.ktor.client.statement.*
import kjob.core.job.JobProps
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory
import java.time.Instant

@Single
class ActivityPubNoteServiceImpl(
        private val httpClient: HttpClient,
        private val jobQueueParentService: JobQueueParentService,
        private val userService: IUserService,
        private val postRepository: IPostRepository,
        private val activityPubUserService: ActivityPubUserService
) : ActivityPubNoteService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun createNote(post: Post) {
        val followers = userService.findFollowersById(post.userId)
        val userEntity = userService.findById(post.userId)
        val note = Config.configData.objectMapper.writeValueAsString(post)
        followers.forEach { followerEntity ->
            jobQueueParentService.schedule(DeliverPostJob) {
                props[it.actor] = userEntity.url
                props[it.post] = note
                props[it.inbox] = followerEntity.inbox
            }
        }
    }

    override suspend fun createNoteJob(props: JobProps<DeliverPostJob>) {
        val actor = props[DeliverPostJob.actor]
        val postEntity = Config.configData.objectMapper.readValue<Post>(props[DeliverPostJob.post])
        val note = Note(
                name = "Note",
                id = postEntity.url,
                attributedTo = actor,
                content = postEntity.text,
                published = Instant.ofEpochMilli(postEntity.createdAt).toString(),
                to = listOf(public, actor + "/follower")
        )
        val inbox = props[DeliverPostJob.inbox]
        logger.debug("createNoteJob: actor={}, note={}, inbox={}", actor, postEntity, inbox)
        httpClient.postAp(
                urlString = inbox,
                username = "$actor#pubkey",
                jsonLd = Create(
                        name = "Create Note",
                        `object` = note,
                        actor = note.attributedTo,
                        id = "${Config.configData.url}/create/note/${postEntity.id}"
                )
        )
    }

    override suspend fun fetchNote(url: String, targetActor: String?): Note {
        val post = postRepository.findByUrl(url)
        if (post != null) {
            return postToNote(post)
        }
        val response = httpClient.getAp(
                url, targetActor?.let { "$targetActor#pubkey" }
        )
        val note = Config.configData.objectMapper.readValue<Note>(response.bodyAsText())
        return note(note, targetActor, url)
    }

    private suspend fun postToNote(post: Post): Note {
        val user = userService.findById(post.userId)
        val reply = post.replyId?.let { postRepository.findOneById(it) }
        return Note(
                name = "Post",
                id = post.apId,
                attributedTo = user.url,
                content = post.text,
                published = Instant.ofEpochMilli(post.createdAt).toString(),
                to = listOf(public, user.url + "/follower"),
                sensitive = post.sensitive,
                cc = listOf(public, user.url + "/follower"),
                inReplyTo = reply?.url
        )
    }

    private suspend fun ActivityPubNoteServiceImpl.note(
            note: Note,
            targetActor: String?,
            url: String
    ): Note {
        val findByApId = postRepository.findByApId(url)
        if (findByApId != null) {
            return postToNote(findByApId)
        }
        val person = activityPubUserService.fetchPerson(
                note.attributedTo ?: throw IllegalActivityPubObjectException("note.attributedTo is null"),
                targetActor
        )
        val user =
                userService.findByUrl(person.url ?: throw IllegalActivityPubObjectException("person.url is null"))

        val visibility =
                if (note.to.contains(public) && note.cc.contains(public)) {
                    Visibility.PUBLIC
                } else if (note.to.find { it.endsWith("/followers") } != null && note.cc.contains(public)) {
                    Visibility.UNLISTED
                } else if (note.to.find { it.endsWith("/followers") } != null) {
                    Visibility.FOLLOWERS
                } else {
                    Visibility.DIRECT
                }

        val reply = note.inReplyTo?.let {
            fetchNote(it, targetActor)
            postRepository.findByUrl(it)
        }

        postRepository.save(
                Post(
                        id = postRepository.generateId(),
                        userId = user.id,
                        overview = null,
                        text = note.content.orEmpty(),
                        createdAt = Instant.parse(note.published).toEpochMilli(),
                        visibility = visibility,
                        url = note.id ?: url,
                        repostId = null,
                        replyId = reply?.id,
                        sensitive = note.sensitive,
                        apId = note.id ?: url,
                )
        )
        return note
    }

    override suspend fun fetchNote(note: Note, targetActor: String?): Note =
            note(note, targetActor, note.id ?: throw IllegalArgumentException("note.id is null"))

    companion object {
        const val public: String = "https://www.w3.org/ns/activitystreams#Public"
    }
}
