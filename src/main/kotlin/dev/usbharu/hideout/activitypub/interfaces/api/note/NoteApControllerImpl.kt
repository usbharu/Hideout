package dev.usbharu.hideout.activitypub.interfaces.api.note

import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.service.objects.note.NoteApApiService
import dev.usbharu.hideout.core.infrastructure.springframework.httpsignature.HttpSignatureUser
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class NoteApControllerImpl(private val noteApApiService: NoteApApiService) : NoteApController {
    override suspend fun postsAp(
        @PathVariable(value = "postId") postId: Long,
    ): ResponseEntity<Note> {
        val context = SecurityContextHolder.getContext()
        val userId =
            if (context.authentication is PreAuthenticatedAuthenticationToken &&
                context.authentication.details is HttpSignatureUser
            ) {
                (context.authentication.details as HttpSignatureUser).id
            } else {
                null
            }

        val note = noteApApiService.getNote(postId, userId)
        if (note != null) {
            return ResponseEntity.ok(note)
        }
        return ResponseEntity.notFound().build()
    }
}
