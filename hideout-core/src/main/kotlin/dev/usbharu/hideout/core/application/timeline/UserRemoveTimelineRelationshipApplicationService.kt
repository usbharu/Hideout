package dev.usbharu.hideout.core.application.timeline

import dev.usbharu.hideout.core.application.exception.PermissionDeniedException
import dev.usbharu.hideout.core.application.shared.LocalUserAbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserRemoveTimelineRelationshipApplicationService(
    transaction: Transaction,
    private val timelineRelationshipRepository: TimelineRelationshipRepository,
    private val timelineRepository: TimelineRepository
) :
    LocalUserAbstractApplicationService<RemoveTimelineRelationship, Unit>(
        transaction,
        logger
    ) {

    override suspend fun internalExecute(command: RemoveTimelineRelationship, principal: LocalUser) {
        val timelineRelationship = (
            timelineRelationshipRepository.findById(command.timelineRelationshipId)
                ?: throw IllegalArgumentException("TimelineRelationship ${command.timelineRelationshipId} not found.")
            )

        val timeline = (
            timelineRepository.findById(timelineRelationship.timelineId)
                ?: throw IllegalArgumentException("Timeline ${timelineRelationship.timelineId} not found.")
            )

        if (timeline.userDetailId != principal.userDetailId) {
            throw PermissionDeniedException()
        }

        timelineRelationshipRepository.delete(timelineRelationship)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserRemoveTimelineRelationshipApplicationService::class.java)
    }
}
