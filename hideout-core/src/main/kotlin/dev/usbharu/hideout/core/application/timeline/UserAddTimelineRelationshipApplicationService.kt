package dev.usbharu.hideout.core.application.timeline

import dev.usbharu.hideout.core.application.shared.LocalUserAbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserAddTimelineRelationshipApplicationService(
    private val timelineRelationshipRepository: TimelineRelationshipRepository,
    transaction: Transaction
) :
    LocalUserAbstractApplicationService<AddTimelineRelationship, Unit>(
        transaction, logger
    ) {
    override suspend fun internalExecute(command: AddTimelineRelationship, principal: LocalUser) {
        timelineRelationshipRepository.save(command.timelineRelationship)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserAddTimelineRelationshipApplicationService::class.java)
    }
}