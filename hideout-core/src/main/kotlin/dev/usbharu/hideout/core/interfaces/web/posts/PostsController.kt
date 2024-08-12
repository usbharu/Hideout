package dev.usbharu.hideout.core.interfaces.web.posts

import dev.usbharu.hideout.core.application.instance.GetLocalInstanceApplicationService
import dev.usbharu.hideout.core.application.post.GetPostDetail
import dev.usbharu.hideout.core.application.post.GetPostDetailApplicationService
import dev.usbharu.hideout.core.infrastructure.springframework.SpringSecurityFormLoginPrincipalContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class PostsController(
    private val getPostDetailApplicationService: GetPostDetailApplicationService,
    private val springSecurityFormLoginPrincipalContextHolder: SpringSecurityFormLoginPrincipalContextHolder,
    private val getLocalInstanceApplicationService: GetLocalInstanceApplicationService
) {
    @GetMapping("/users/{name}/posts/{id}")
    suspend fun postById(@PathVariable id: Long, model: Model): String {
        val principal = springSecurityFormLoginPrincipalContextHolder.getPrincipal()
        val post = getPostDetailApplicationService.execute(GetPostDetail(id), principal)
        val instance = getLocalInstanceApplicationService.execute(Unit, principal)
        model.addAttribute("post", post)
        model.addAttribute("instance", instance)
        return "postById"
    }
}