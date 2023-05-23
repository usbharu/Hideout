package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ActivityPubStringResponse
import dev.usbharu.hideout.domain.model.ap.Create
import dev.usbharu.hideout.domain.model.ap.Note
import dev.usbharu.hideout.exception.ap.IllegalActivityPubObjectException
import io.ktor.http.*
import org.koin.core.annotation.Single

@Single
class ActivityPubCreateServiceImpl(
    private val activityPubNoteService: ActivityPubNoteService
) : ActivityPubCreateService {
    override suspend fun receiveCreate(create: Create): ActivityPubResponse {
        val value = create.`object` ?: throw IllegalActivityPubObjectException("object is null")
        if (value.type.contains("Note").not()) {
            throw IllegalActivityPubObjectException("object is not Note")
        }

        val note = value as Note
        activityPubNoteService.fetchNote(note)
        return ActivityPubStringResponse(HttpStatusCode.Created, "Created")
    }
}
