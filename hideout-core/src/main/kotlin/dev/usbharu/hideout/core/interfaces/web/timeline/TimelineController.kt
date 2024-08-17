package dev.usbharu.hideout.core.interfaces.web.timeline

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.application.timeline.ReadTimeline
import dev.usbharu.hideout.core.application.timeline.ReadTimelineApplicationService
import dev.usbharu.hideout.core.domain.model.support.page.Page
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.infrastructure.springframework.SpringSecurityFormLoginPrincipalContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class TimelineController(
    private val readTimelineApplicationService: ReadTimelineApplicationService,
    private val userDetailRepository: UserDetailRepository,
    private val springSecurityFormLoginPrincipalContextHolder: SpringSecurityFormLoginPrincipalContextHolder,
    private val transaction: Transaction
) {
    @GetMapping("/home")
    suspend fun homeTimeline(
        model: Model,
        @RequestParam sinceId: String?,
        @RequestParam maxId: String?,
        @RequestParam minId: String?
    ): String {
        val principal = springSecurityFormLoginPrincipalContextHolder.getPrincipal()
        val userDetail = transaction.transaction {
            userDetailRepository.findByActorId(principal.actorId.id)
                ?: throw InternalServerException("UserDetail not found.")
        }

        val homeTimelineId = userDetail.homeTimelineId!!
        val execute = readTimelineApplicationService.execute(
            ReadTimeline(
                timelineId = homeTimelineId.value,
                mediaOnly = false,
                localOnly = false,
                remoteOnly = false,
                page = Page.of(
                    maxId = maxId?.toLongOrNull(),
                    sinceId = sinceId?.toLongOrNull(),
                    minId = minId?.toLongOrNull(),
                    limit = 20
                )
            ),
            principal
        )

        model.addAttribute("timeline", execute)

        return "homeTimeline"
    }
}
