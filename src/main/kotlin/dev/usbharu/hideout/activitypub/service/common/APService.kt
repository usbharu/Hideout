package dev.usbharu.hideout.activitypub.service.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.activitypub.domain.exception.JsonParseException
import dev.usbharu.hideout.activitypub.interfaces.api.common.ActivityPubResponse
import dev.usbharu.hideout.activitypub.interfaces.api.common.ActivityPubStringResponse
import dev.usbharu.hideout.core.external.job.InboxJob
import dev.usbharu.hideout.core.service.job.JobQueueParentService
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
    @Qualifier("activitypub") private val objectMapper: ObjectMapper,
    private val jobQueueParentService: JobQueueParentService
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
        jobQueueParentService.schedule(InboxJob) {
            props[it.json] = json
            props[it.type] = type.name
        }
        return ActivityPubStringResponse(message = "")
    }
}
