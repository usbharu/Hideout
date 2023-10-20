package dev.usbharu.hideout.service.ap

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.ApplicationConfig
import dev.usbharu.hideout.domain.model.ap.Create
import dev.usbharu.hideout.domain.model.ap.Document
import dev.usbharu.hideout.domain.model.ap.Note
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility
import dev.usbharu.hideout.domain.model.job.DeliverPostJob
import dev.usbharu.hideout.exception.FailedToGetResourcesException
import dev.usbharu.hideout.exception.ap.FailedToGetActivityPubResourceException
import dev.usbharu.hideout.exception.ap.IllegalActivityPubObjectException
import dev.usbharu.hideout.query.FollowerQueryService
import dev.usbharu.hideout.query.MediaQueryService
import dev.usbharu.hideout.query.PostQueryService
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.repository.PostRepository
import dev.usbharu.hideout.service.ap.resource.APResourceResolveService
import dev.usbharu.hideout.service.ap.resource.resolve
import dev.usbharu.hideout.service.core.Transaction
import dev.usbharu.hideout.service.job.JobQueueParentService
import dev.usbharu.hideout.service.post.PostCreateInterceptor
import dev.usbharu.hideout.service.post.PostService
import io.ktor.client.plugins.*
import kjob.core.job.JobProps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.Instant

interface APNoteService {

    suspend fun createNote(post: Post)
    suspend fun createNoteJob(props: JobProps<DeliverPostJob>)

    @Cacheable("fetchNote")
    fun fetchNoteAsync(url: String, targetActor: String? = null): Deferred<Note> {
        return CoroutineScope(Dispatchers.IO).async {
            newSuspendedTransaction {
                fetchNote(url, targetActor)
            }
        }
    }

    suspend fun fetchNote(url: String, targetActor: String? = null): Note
    suspend fun fetchNote(note: Note, targetActor: String? = null): Note
}

@Service
@Suppress("LongParameterList")
class APNoteServiceImpl(
    private val jobQueueParentService: JobQueueParentService,
    private val postRepository: PostRepository,
    private val apUserService: APUserService,
    private val userQueryService: UserQueryService,
    private val followerQueryService: FollowerQueryService,
    private val postQueryService: PostQueryService,
    private val mediaQueryService: MediaQueryService,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper,
    private val applicationConfig: ApplicationConfig,
    private val postService: PostService,
    private val apResourceResolveService: APResourceResolveService,
    private val apRequestService: APRequestService,
    private val transaction: Transaction

) : APNoteService, PostCreateInterceptor {

    init {
        postService.addInterceptor(this)
    }

    private val logger = LoggerFactory.getLogger(APNoteServiceImpl::class.java)

    override suspend fun createNote(post: Post) {
        logger.info("CREATE Create Local Note ${post.url}")
        logger.debug("START Create Local Note ${post.url}")
        logger.trace("{}", post)
        val followers = followerQueryService.findFollowersById(post.userId)

        logger.debug("DELIVER Deliver Note Create ${followers.size} accounts.")

        val userEntity = userQueryService.findById(post.userId)
        val note = objectMapper.writeValueAsString(post)
        val mediaList = objectMapper.writeValueAsString(mediaQueryService.findByPostId(post.id))
        followers.forEach { followerEntity ->
            jobQueueParentService.schedule(DeliverPostJob) {
                props[DeliverPostJob.actor] = userEntity.url
                props[DeliverPostJob.post] = note
                props[DeliverPostJob.inbox] = followerEntity.inbox
                props[DeliverPostJob.media] = mediaList
            }
        }

        logger.debug("SUCCESS Create Local Note ${post.url}")
    }

    override suspend fun createNoteJob(props: JobProps<DeliverPostJob>) {
        val actor = props[DeliverPostJob.actor]
        val postEntity = objectMapper.readValue<Post>(props[DeliverPostJob.post])
        val mediaList =
            objectMapper.readValue<List<dev.usbharu.hideout.domain.model.hideout.entity.Media>>(
                props[DeliverPostJob.media]
            )
        val note = Note(
            name = "Note",
            id = postEntity.url,
            attributedTo = actor,
            content = postEntity.text,
            published = Instant.ofEpochMilli(postEntity.createdAt).toString(),
            to = listOf(public, "$actor/follower"),
            attachment = mediaList.map { Document(mediaType = "image/jpeg", url = it.url) }

        )
        val inbox = props[DeliverPostJob.inbox]
        logger.debug("createNoteJob: actor={}, note={}, inbox={}", actor, postEntity, inbox)

        transaction.transaction {
            val signer = userQueryService.findByUrl(actor)
            apRequestService.apPost(
                inbox,
                Create(
                    name = "Create Note",
                    `object` = note,
                    actor = note.attributedTo,
                    id = "${applicationConfig.url}/create/note/${postEntity.id}"
                ),
                signer
            )
        }
    }

    override suspend fun fetchNote(url: String, targetActor: String?): Note {
        logger.debug("START Fetch Note url: {}", url)
        try {
            val post = postQueryService.findByUrl(url)
            logger.debug("SUCCESS Found in local url: {}", url)
            return postToNote(post)
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

    private suspend fun postToNote(post: Post): Note {
        val user = userQueryService.findById(post.userId)
        val reply = post.replyId?.let { postQueryService.findById(it) }
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

    private suspend fun saveIfMissing(
        note: Note,
        targetActor: String?,
        url: String
    ): Note {
        if (note.id == null) {
            throw IllegalArgumentException("id is null")
//            return internalNote(note, targetActor, url)
        }

        val findByApId = try {
            postQueryService.findByApId(note.id!!)
        } catch (_: FailedToGetResourcesException) {
            return saveNote(note, targetActor, url)
        }
        return postToNote(findByApId)
    }

    private suspend fun saveNote(note: Note, targetActor: String?, url: String): Note {
        val person = apUserService.fetchPersonWithEntity(
            note.attributedTo ?: throw IllegalActivityPubObjectException("note.attributedTo is null"),
            targetActor
        )

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
            postQueryService.findByUrl(it)
        }

        // TODO: リモートのメディア処理を追加
        postService.createRemote(
            Post.of(
                id = postRepository.generateId(),
                userId = person.second.id,
                text = note.content.orEmpty(),
                createdAt = Instant.parse(note.published).toEpochMilli(),
                visibility = visibility,
                url = note.id ?: url,
                replyId = reply?.id,
                sensitive = note.sensitive,
                apId = note.id ?: url,
            )
        )
        return note
    }

    override suspend fun fetchNote(note: Note, targetActor: String?): Note =
        saveIfMissing(note, targetActor, note.id ?: throw IllegalArgumentException("note.id is null"))

    override suspend fun run(post: Post) {
        createNote(post)
    }

    companion object {
        const val public: String = "https://www.w3.org/ns/activitystreams#Public"
    }
}
