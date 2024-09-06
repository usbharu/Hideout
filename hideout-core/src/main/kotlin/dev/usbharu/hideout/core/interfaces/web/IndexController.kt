package dev.usbharu.hideout.core.interfaces.web

import dev.usbharu.hideout.core.application.instance.GetLocalInstanceApplicationService
import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.infrastructure.springframework.SpringSecurityFormLoginPrincipalContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class IndexController(
    private val applicationConfig: ApplicationConfig,
    private val springSecurityFormLoginPrincipalContextHolder: SpringSecurityFormLoginPrincipalContextHolder,
    private val getLocalInstanceApplicationService: GetLocalInstanceApplicationService
) {
    @GetMapping("/")
    suspend fun index(model: Model): String {
        if (springSecurityFormLoginPrincipalContextHolder.getPrincipal().userDetailId != null) {
            return "redirect:/home"
        }

        val instance = getLocalInstanceApplicationService.execute(Unit, Anonymous)
        model.addAttribute("instance", instance)
        model.addAttribute("applicationConfig", applicationConfig)
        return "top"
    }
}
