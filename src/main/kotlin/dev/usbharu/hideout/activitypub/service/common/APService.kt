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

package dev.usbharu.hideout.activitypub.service.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.activitypub.domain.exception.JsonParseException
import dev.usbharu.hideout.core.external.job.InboxJob
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import dev.usbharu.httpsignature.common.HttpRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

interface APService {
    fun parseActivity(json: String): ActivityType

    suspend fun processActivity(
        json: String,
        type: ActivityType,
        httpRequest: HttpRequest,
        map: Map<String, List<String>>
    )
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

    override suspend fun processActivity(
        json: String,
        type: ActivityType,
        httpRequest: HttpRequest,
        map: Map<String, List<String>>
    ) {
        logger.debug("process activity: {}", type)
        jobQueueParentService.schedule(InboxJob) {
            props[it.json] = json
            props[it.type] = type.name
            val writeValueAsString = objectMapper.writeValueAsString(httpRequest)
            props[it.httpRequest] = writeValueAsString
            props[it.headers] = objectMapper.writeValueAsString(map)
        }
        return
    }
}
