package dev.usbharu.hideout.activitypub.service.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.exception.JsonParseException
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.interfaces.api.common.ActivityPubResponse
import dev.usbharu.hideout.activitypub.service.activity.accept.APAcceptService
import dev.usbharu.hideout.activitypub.service.activity.create.APCreateService
import dev.usbharu.hideout.activitypub.service.activity.delete.APReceiveDeleteService
import dev.usbharu.hideout.activitypub.service.activity.follow.APReceiveFollowService
import dev.usbharu.hideout.activitypub.service.activity.like.APLikeService
import dev.usbharu.hideout.activitypub.service.activity.undo.APUndoService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

interface APService {
    fun parseActivity(json: String): ActivityType

    suspend fun processActivity(json: String, type: ActivityType): ActivityPubResponse?
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

@Service
class APServiceImpl(
    private val apReceiveFollowService: APReceiveFollowService,
    private val apUndoService: APUndoService,
    private val apAcceptService: APAcceptService,
    private val apCreateService: APCreateService,
    private val apLikeService: APLikeService,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper,
    private val apReceiveDeleteService: APReceiveDeleteService
) : APService {

    val logger: Logger = LoggerFactory.getLogger(APServiceImpl::class.java)
    override fun parseActivity(json: String): ActivityType {
        val readTree = try {
            objectMapper.readTree(json)
        } catch (e: com.fasterxml.jackson.core.JsonParseException) {
            throw JsonParseException("Failed to parse json", e)
        }
        logger.trace(
            """
            |
            |***** Trace Begin Activity *****
            |
            |{}
            |
            |***** Trace End Activity *****
            |
            """.trimMargin(),
            readTree.toPrettyString()
        )
        if (readTree.isObject.not()) {
            throw JsonParseException("Json is not object.")
        }
        val type = readTree["type"] ?: throw JsonParseException("Type is null")
        if (type.isArray) {
            try {
                return type.firstNotNullOf { jsonNode: JsonNode ->
                    ActivityType.values().firstOrNull { it.name.equals(jsonNode.asText(), true) }
                }
            } catch (e: NoSuchElementException) {
                throw IllegalArgumentException("No valid TYPE", e)
            }
        }
        try {
            return ActivityType.values().first { it.name.equals(type.asText(), true) }
        } catch (e: NoSuchElementException) {
            throw IllegalArgumentException("No valid TYPE", e)
        }
    }

    @Suppress("CyclomaticComplexMethod", "NotImplementedDeclaration")
    override suspend fun processActivity(json: String, type: ActivityType): ActivityPubResponse {
        logger.debug("process activity: {}", type)
        return when (type) {
            ActivityType.Accept -> apAcceptService.receiveAccept(objectMapper.readValue(json))
            ActivityType.Follow ->
                apReceiveFollowService
                    .receiveFollow(objectMapper.readValue(json, Follow::class.java))

            ActivityType.Create -> apCreateService.receiveCreate(objectMapper.readValue(json))
            ActivityType.Like -> apLikeService.receiveLike(objectMapper.readValue(json))
            ActivityType.Undo -> apUndoService.receiveUndo(objectMapper.readValue(json))
            ActivityType.Delete -> apReceiveDeleteService.receiveDelete(objectMapper.readValue(json))

            else -> {
                throw IllegalArgumentException("$type is not supported.")
            }
        }
    }
}
