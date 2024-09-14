package dev.usbharu.hideout.core.application.timeline

import dev.usbharu.hideout.core.application.shared.LocalUserAbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.model.timeline.*
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UserRegisterTimelineApplicationService(
    private val idGenerateService: IdGenerateService,
    private val timelineRepository: TimelineRepository,
    transaction: Transaction
) :
    LocalUserAbstractApplicationService<RegisterTimeline, TimelineId>(transaction, logger) {
    override suspend fun internalExecute(command: RegisterTimeline, principal: LocalUser): TimelineId {
        val timeline = Timeline.create(
            id = TimelineId(idGenerateService.generateId()),
            userDetailId = principal.userDetailId,
            name = TimelineName(command.timelineName),
            visibility = command.visibility,
            isSystem = false
        )

        timelineRepository.save(timeline)
        return timeline.id
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserRegisterTimelineApplicationService::class.java)
    }
}

data class RegisterTimeline(
    val timelineName: String,
    val visibility: TimelineVisibility
)