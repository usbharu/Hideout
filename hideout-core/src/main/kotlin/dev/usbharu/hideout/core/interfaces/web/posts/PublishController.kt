package dev.usbharu.hideout.core.interfaces.web.posts

import dev.usbharu.hideout.core.application.actor.GetUserDetail
import dev.usbharu.hideout.core.application.actor.GetUserDetailApplicationService
import dev.usbharu.hideout.core.application.instance.GetLocalInstanceApplicationService
import dev.usbharu.hideout.core.infrastructure.springframework.SpringSecurityFormLoginPrincipalContextHolder
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PublishController(
    private val getLocalInstanceApplicationService: GetLocalInstanceApplicationService,
    private val springSecurityFormLoginPrincipalContextHolder: SpringSecurityFormLoginPrincipalContextHolder,
    private val getUserDetailApplicationService: GetUserDetailApplicationService
) {
    @GetMapping("/publish")
    suspend fun publish(model: Model): String {
        val principal = springSecurityFormLoginPrincipalContextHolder.getPrincipal()

        if (principal.userDetailId == null) {
            throw AccessDeniedException("403 Forbidden")
        }

        val instance = getLocalInstanceApplicationService.execute(Unit, principal)
        val userDetail = getUserDetailApplicationService.execute(GetUserDetail(principal.userDetailId!!.id), principal)
        model.addAttribute("instance", instance)
        model.addAttribute("user")
        return "post-postForm"
    }
}