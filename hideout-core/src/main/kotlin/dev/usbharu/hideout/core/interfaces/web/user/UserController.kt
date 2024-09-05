package dev.usbharu.hideout.core.interfaces.web.user

import dev.usbharu.hideout.core.application.actor.GetActorDetail
import dev.usbharu.hideout.core.application.actor.GetActorDetailApplicationService
import dev.usbharu.hideout.core.application.instance.GetLocalInstanceApplicationService
import dev.usbharu.hideout.core.application.timeline.GetUserTimeline
import dev.usbharu.hideout.core.application.timeline.GetUserTimelineApplicationService
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.page.Page
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.infrastructure.springframework.SpringSecurityFormLoginPrincipalContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
class UserController(
    private val getLocalInstanceApplicationService: GetLocalInstanceApplicationService,
    private val getUserDetailApplicationService: GetActorDetailApplicationService,
    private val springSecurityFormLoginPrincipalContextHolder: SpringSecurityFormLoginPrincipalContextHolder,
    private val getUserTimelineApplicationService: GetUserTimelineApplicationService
) {
    @GetMapping("/users/{name}")
    suspend fun userById(
        @PathVariable name: String,
        @RequestParam("min_id") minId: Long?,
        @RequestParam("max_id") maxId: Long?,
        @RequestParam("since_id") sinceId: Long?,
        model: Model
    ): String {
        val principal = springSecurityFormLoginPrincipalContextHolder.getPrincipal()

        model.addAttribute("instance", getLocalInstanceApplicationService.execute(Unit, Anonymous))
        val actorDetail = getUserDetailApplicationService.execute(GetActorDetail(Acct.of(name)), principal)
        model.addAttribute(
            "user",
            actorDetail
        )
        model.addAttribute(
            "userTimeline",
            getUserTimelineApplicationService.execute(
                GetUserTimeline(
                    actorDetail.id,
                    Page.of(maxId, sinceId, minId, 20)
                ),
                principal
            )
        )
        return "userById"
    }
}
