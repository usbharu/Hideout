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

package dev.usbharu.hideout.core.service.notification

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.notification.Notification
import dev.usbharu.hideout.core.domain.model.notification.NotificationRepository
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class NotificationServiceImpl(
    private val relationshipNotificationManagementService: RelationshipNotificationManagementService,
    private val relationshipRepository: RelationshipRepository,
    private val notificationStoreList: List<NotificationStore>,
    private val notificationRepository: NotificationRepository,
    private val actorRepository: ActorRepository,
    private val postRepository: PostRepository,
    private val reactionRepository: ReactionRepository,
    private val applicationConfig: ApplicationConfig
) : NotificationService {
    override suspend fun publishNotify(notificationRequest: NotificationRequest): Notification? {
        logger.debug("NOTIFICATION REQUEST user: {} type: {}", notificationRequest.userId, notificationRequest.type)
        logger.trace("NotificationRequest: {}", notificationRequest)

        val user = actorRepository.findById(notificationRequest.userId)
        if (user == null || user.domain != applicationConfig.url.host) {
            logger.debug("NOTIFICATION REQUEST is rejected. (Remote Actor or user not found.)")
            return null
        }

        // とりあえず個人間のRelationshipに基づいてきめる。今後増やす
        if (!relationship(notificationRequest)) {
            logger.debug("NOTIFICATION REQUEST is rejected. (relationship)")
            return null
        }

        val id = notificationRepository.generateId()
        val createdAt = Instant.now()

        val notification = notificationRequest.buildNotification(id, createdAt)

        val savedNotification = notificationRepository.save(notification)

        val sourceActor = savedNotification.sourceActorId?.let { actorRepository.findById(it) }

        val post = savedNotification.postId?.let { postRepository.findById(it) }
        val reaction = savedNotification.reactionId?.let { reactionRepository.findById(it) }

        logger.info(
            "NOTIFICATION id: {} user: {} type: {}",
            savedNotification.id,
            savedNotification.userId,
            savedNotification.type
        )

        logger.debug("push to {} notification store.", notificationStoreList.size)
        for (it in notificationStoreList) {
            @Suppress("TooGenericExceptionCaught")
            try {
                it.publishNotification(savedNotification, user, sourceActor, post, reaction)
            } catch (e: Exception) {
                logger.warn("FAILED Publish to notification.", e)
            }
        }
        logger.debug("SUCCESS Notification id: {}", savedNotification.id)

        return savedNotification
    }

    override suspend fun unpublishNotify(notificationId: Long) {
        notificationRepository.deleteById(notificationId)
        for (notificationStore in notificationStoreList) {
            notificationStore.unpulishNotification(notificationId)
        }
    }

    /**
     * 個人間のRelationshipに基づいて通知を送信するか判断します
     *
     * @param notificationRequest
     * @return trueの場合送信する
     */
    private suspend fun relationship(notificationRequest: NotificationRequest): Boolean {
        val targetActorId = notificationRequest.sourceActorId ?: return true
        val relationship =
            relationshipRepository.findByUserIdAndTargetUserId(notificationRequest.userId, targetActorId) ?: return true
        return relationshipNotificationManagementService.sendNotification(relationship, notificationRequest)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NotificationServiceImpl::class.java)
    }
}
