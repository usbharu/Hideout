package dev.usbharu.hideout.core.service.notification

import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.notification.Notification
import dev.usbharu.hideout.core.domain.model.notification.NotificationRepository
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
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
    private val reactionRepository: ReactionRepository
) : NotificationService {
    @Suppress("ReplaceNotNullAssertionWithElvisReturn")
    override suspend fun publishNotify(notificationRequest: NotificationRequest): Notification? {

        // とりあえず個人間のRelationshipに基づいてきめる。今後増やす
        if (!relationship(notificationRequest)) {
            return null
        }

        val id = notificationRepository.generateId()
        val createdAt = Instant.now()

        val notification = notificationRequest.buildNotification(id, createdAt)

        val savedNotification = notificationRepository.save(notification)

        // saveで参照整合性違反が発生するはずなので
        val user = actorRepository.findById(savedNotification.userId)!!
        val sourceActor = savedNotification.sourceActorId?.let { actorRepository.findById(it) }

        val post = savedNotification.postId?.let { postRepository.findById(it) }
        val reaction = savedNotification.reactionId?.let { reactionRepository.findById(it) }

        for (it in notificationStoreList) {
            it.publishNotification(savedNotification, user, sourceActor, post, reaction)
        }

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
}
