package dev.usbharu.hideout.core.application.timeline

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository
import dev.usbharu.hideout.core.external.timeline.TimelineStore
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SetTimelineToTimelineStoreApplicationService(
    transaction: Transaction,
    private val timelineStore: TimelineStore,
    private val timelineRepository: TimelineRepository
) :
    AbstractApplicationService<SetTimleineStore, Unit>(
        transaction, logger
    ) {
    override suspend fun internalExecute(command: SetTimleineStore, principal: Principal) {
        val findById = timelineRepository.findById(command.timelineId)
            ?: throw IllegalArgumentException("Timeline ${command.timelineId} not found")
        timelineStore.addTimeline(findById, emptyList())
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SetTimelineToTimelineStoreApplicationService::class.java)
    }
}