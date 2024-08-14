package dev.usbharu.hideout.core.application.timeline

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.support.page.PaginationList
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.model.support.timelineobjectdetail.TimelineObjectDetail
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository
import dev.usbharu.hideout.core.external.timeline.ReadTimelineOption
import dev.usbharu.hideout.core.external.timeline.TimelineStore
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReadTimelineApplicationService(
    private val timelineStore: TimelineStore,
    private val timelineRepository: TimelineRepository,
    transaction: Transaction
) :
    AbstractApplicationService<ReadTimeline, PaginationList<TimelineObjectDetail, PostId>>(transaction, logger) {
    override suspend fun internalExecute(
        command: ReadTimeline,
        principal: Principal
    ): PaginationList<TimelineObjectDetail, PostId> {
        val findById = timelineRepository.findById(TimelineId(command.timelineId))
            ?: throw IllegalArgumentException("Timeline ${command.timelineId} not found.")

        val readTimelineOption = ReadTimelineOption(
            command.mediaOnly,
            command.localOnly,
            command.remoteOnly
        )

        return timelineStore.readTimeline(
            findById,
            readTimelineOption,
            command.page,
            principal,
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ReadTimelineApplicationService::class.java)
    }
}