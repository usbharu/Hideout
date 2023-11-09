package dev.usbharu.hideout.activitypub.interfaces.api.note

import dev.usbharu.hideout.activitypub.domain.model.Note
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

interface NoteApController {
    @GetMapping("/users/*/posts/{postId}")
    suspend fun postsAp(
        @PathVariable("postId") postId: Long
    ): ResponseEntity<Note>
}
