package dev.usbharu.hideout.controller

import dev.usbharu.hideout.domain.model.ap.Note
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

interface NoteApController {
    @GetMapping("/users/*/posts/{postId}")
    suspend fun postsAp(
        @PathVariable("postId") postId: Long,
        @CurrentSecurityContext context: SecurityContext
    ): ResponseEntity<Note>
}
