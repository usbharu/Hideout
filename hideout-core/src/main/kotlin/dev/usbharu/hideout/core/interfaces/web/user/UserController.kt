package dev.usbharu.hideout.core.interfaces.web.user

import dev.usbharu.hideout.core.application.actor.GetActorDetail
import dev.usbharu.hideout.core.application.actor.GetActorDetailApplicationService
import dev.usbharu.hideout.core.application.instance.GetLocalInstanceApplicationService
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.infrastructure.springframework.SpringSecurityFormLoginPrincipalContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class UserController(
    private val getLocalInstanceApplicationService: GetLocalInstanceApplicationService,
    private val getUserDetailApplicationService: GetActorDetailApplicationService,
    private val springSecurityFormLoginPrincipalContextHolder: SpringSecurityFormLoginPrincipalContextHolder,
) {
    @GetMapping("/users/{name}")
    suspend fun userById(@PathVariable name: String, model: Model): String {
        val principal = springSecurityFormLoginPrincipalContextHolder.getPrincipal()

        model.addAttribute("instance", getLocalInstanceApplicationService.execute(Unit, Anonymous))
        model.addAttribute(
            "user",
            getUserDetailApplicationService.execute(GetActorDetail(Acct.of(name)), principal)
        )
        return "userById"
    }
}