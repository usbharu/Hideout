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

package dev.usbharu.hideout.activitypub.service.activity.like

import dev.usbharu.hideout.activitypub.domain.exception.FailedToGetActivityPubResourceException
import dev.usbharu.hideout.activitypub.domain.model.Emoji
import dev.usbharu.hideout.activitypub.domain.model.Like
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.activitypub.service.objects.emoji.EmojiService
import dev.usbharu.hideout.activitypub.service.objects.note.APNoteService
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.emoji.UnicodeEmoji
import dev.usbharu.hideout.core.service.reaction.ReactionService
import org.springframework.stereotype.Service

@Service
class APLikeProcessor(
    transaction: Transaction,
    private val apUserService: APUserService,
    private val apNoteService: APNoteService,
    private val reactionService: ReactionService,
    private val emojiService: EmojiService
) :
    AbstractActivityPubProcessor<Like>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Like>) {
        val actor = activity.activity.actor
        val content = activity.activity.content

        val target = activity.activity.apObject

        val personWithEntity = apUserService.fetchPersonWithEntity(actor)

        try {
            val post = apNoteService.fetchNoteWithEntity(target).second

            val emoji = if (content.startsWith(":")) {
                val tag = activity.activity.tag
                (tag.firstOrNull { it is Emoji } as? Emoji)?.let { emojiService.fetchEmoji(it).second }
            } else {
                UnicodeEmoji(content)
            }

            reactionService.receiveReaction(
                emoji ?: UnicodeEmoji("❤"),
                personWithEntity.second.id,
                post.id
            )

            logger.debug("SUCCESS Add Like($content) from ${personWithEntity.second.url} to ${post.url}")
        } catch (e: FailedToGetActivityPubResourceException) {
            logger.debug("FAILED failed to get {}", target)
            logger.trace("", e)
            return
        }
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Like

    override fun type(): Class<Like> = Like::class.java
}
