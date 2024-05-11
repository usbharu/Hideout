/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
