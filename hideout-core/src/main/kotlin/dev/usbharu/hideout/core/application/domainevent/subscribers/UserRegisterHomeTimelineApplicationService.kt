package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.model.timeline.*
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UserRegisterHomeTimelineApplicationService(
    private val userDetailRepository: UserDetailRepository,
    private val timelineRepository: TimelineRepository,
    private val idGenerateService: IdGenerateService, transaction: Transaction,
) : AbstractApplicationService<RegisterHomeTimeline, Unit>(transaction, logger) {
    override suspend fun internalExecute(command: RegisterHomeTimeline, principal: Principal) {

        val userDetail = (userDetailRepository.findById(UserDetailId(command.userDetailId))
            ?: throw IllegalArgumentException("UserDetail ${command.userDetailId} not found."))

        val timeline = Timeline.create(
            TimelineId(idGenerateService.generateId()),
            UserDetailId(command.userDetailId),
            TimelineName("System-LocalUser-HomeTimeline-${command.userDetailId}"),
            TimelineVisibility.PRIVATE,
            true
        )
        timelineRepository.save(timeline)
        userDetail.homeTimelineId = timeline.id

        userDetailRepository.save(userDetail)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserRegisterHomeTimelineApplicationService::class.java)
    }
}