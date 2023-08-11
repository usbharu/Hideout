package dev.usbharu.hideout.service.ap

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ap.Follow
import dev.usbharu.hideout.domain.model.job.*
import dev.usbharu.hideout.exception.JsonParseException
import kjob.core.dsl.JobContextWithProps
import kjob.core.job.JobProps
import org.koin.core.annotation.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface APService {
    fun parseActivity(json: String): ActivityType

    suspend fun processActivity(json: String, type: ActivityType): ActivityPubResponse?

    suspend fun <T : HideoutJob> processActivity(job: JobContextWithProps<T>, hideoutJob: HideoutJob)
}

enum class ActivityType {
    Accept,
    Add,
    Announce,
    Arrive,
    Block,
    Create,
    Delete,
    Dislike,
    Flag,
    Follow,
    Ignore,
    Invite,
    Join,
    Leave,
    Like,
    Listen,
    Move,
    Offer,
    Question,
    Reject,
    Read,
    Remove,
    TentativeReject,
    TentativeAccept,
    Travel,
    Undo,
    Update,
    View,
    Other
}

enum class ActivityVocabulary {
    Object,
    Link,
    Activity,
    IntransitiveActivity,
    Collection,
    OrderedCollection,
    CollectionPage,
    OrderedCollectionPage,
    Accept,
    Add,
    Announce,
    Arrive,
    Block,
    Create,
    Delete,
    Dislike,
    Flag,
    Follow,
    Ignore,
    Invite,
    Join,
    Leave,
    Like,
    Listen,
    Move,
    Offer,
    Question,
    Reject,
    Read,
    Remove,
    TentativeReject,
    TentativeAccept,
    Travel,
    Undo,
    Update,
    View,
    Application,
    Group,
    Organization,
    Person,
    Service,
    Article,
    Audio,
    Document,
    Event,
    Image,
    Note,
    Page,
    Place,
    Profile,
    Relationship,
    Tombstone,
    Video,
    Mention,
}

enum class ExtendedActivityVocabulary {
    Object,
    Link,
    Activity,
    IntransitiveActivity,
    Collection,
    OrderedCollection,
    CollectionPage,
    OrderedCollectionPage,
    Accept,
    Add,
    Announce,
    Arrive,
    Block,
    Create,
    Delete,
    Dislike,
    Flag,
    Follow,
    Ignore,
    Invite,
    Join,
    Leave,
    Like,
    Listen,
    Move,
    Offer,
    Question,
    Reject,
    Read,
    Remove,
    TentativeReject,
    TentativeAccept,
    Travel,
    Undo,
    Update,
    View,
    Application,
    Group,
    Organization,
    Person,
    Service,
    Article,
    Audio,
    Document,
    Event,
    Image,
    Note,
    Page,
    Place,
    Profile,
    Relationship,
    Tombstone,
    Video,
    Mention,
    Emoji
}

enum class ExtendedVocabulary {
    Emoji
}

@Single
class APServiceImpl(
    private val apReceiveFollowService: APReceiveFollowService,
    private val apNoteService: APNoteService,
    private val apUndoService: APUndoService,
    private val apAcceptService: APAcceptService,
    private val apCreateService: APCreateService,
    private val apLikeService: APLikeService,
    private val apReactionService: APReactionService
) : APService {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    override fun parseActivity(json: String): ActivityType {
        val readTree = Config.configData.objectMapper.readTree(json)
        logger.trace("readTree: {}", readTree)
        if (readTree.isObject.not()) {
            throw JsonParseException("Json is not object.")
        }
        val type = readTree["type"]
        if (type.isArray) {
            return type.firstNotNullOf { jsonNode: JsonNode ->
                ActivityType.values().firstOrNull { it.name.equals(jsonNode.asText(), true) }
            }
        }
        return ActivityType.values().first { it.name.equals(type.asText(), true) }
    }

    @Suppress("CyclomaticComplexMethod", "NotImplementedDeclaration")
    override suspend fun processActivity(json: String, type: ActivityType): ActivityPubResponse {
        logger.debug("proccess activity: {}", type)
        return when (type) {
            ActivityType.Accept -> apAcceptService.receiveAccept(Config.configData.objectMapper.readValue(json))
            ActivityType.Follow -> apReceiveFollowService.receiveFollow(
                Config.configData.objectMapper.readValue(
                    json,
                    Follow::class.java
                )
            )

            ActivityType.Create -> apCreateService.receiveCreate(Config.configData.objectMapper.readValue(json))
            ActivityType.Like -> apLikeService.receiveLike(Config.configData.objectMapper.readValue(json))
            ActivityType.Undo -> apUndoService.receiveUndo(Config.configData.objectMapper.readValue(json))

            else -> {
                throw IllegalArgumentException("$type is not supported.")
            }
        }
    }

    override suspend fun <T : HideoutJob> processActivity(job: JobContextWithProps<T>, hideoutJob: HideoutJob) {
        logger.debug("processActivity: ${hideoutJob.name}")
        when (hideoutJob) {
            ReceiveFollowJob -> apReceiveFollowService.receiveFollowJob(
                job.props as JobProps<ReceiveFollowJob>
            )

            DeliverPostJob -> apNoteService.createNoteJob(job.props as JobProps<DeliverPostJob>)
            DeliverReactionJob -> apReactionService.reactionJob(job.props as JobProps<DeliverReactionJob>)
            DeliverRemoveReactionJob -> apReactionService.removeReactionJob(
                job.props as JobProps<DeliverRemoveReactionJob>
            )
        }
    }
}
