package dev.usbharu.hideout.core.application.timeline

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserAddTimelineRelationshipApplicationService(
    private val timelineRelationshipRepository: TimelineRelationshipRepository,
    transaction: Transaction
) :
    AbstractApplicationService<AddTimelineRelationship, Unit>(
        transaction, logger
    ) {
    override suspend fun internalExecute(command: AddTimelineRelationship) {
        timelineRelationshipRepository.save(command.timelineRelationship)

    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserAddTimelineRelationshipApplicationService::class.java)
    }
}