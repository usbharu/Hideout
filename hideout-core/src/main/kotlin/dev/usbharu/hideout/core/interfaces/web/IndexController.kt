package dev.usbharu.hideout.core.interfaces.web

import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.infrastructure.springframework.SpringSecurityFormLoginPrincipalContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class IndexController(
    private val applicationConfig: ApplicationConfig,
    private val springSecurityFormLoginPrincipalContextHolder: SpringSecurityFormLoginPrincipalContextHolder
) {
    @GetMapping("/")
    suspend fun index(model: Model): String {
        if (springSecurityFormLoginPrincipalContextHolder.getPrincipal().userDetailId != null) {
            return "redirect:/home"
        }


        model.addAttribute("applicationConfig", applicationConfig)
        return "top"
    }
}