package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ActivityPubStringResponse
import dev.usbharu.hideout.domain.model.ap.Create
import dev.usbharu.hideout.domain.model.ap.Note
import dev.usbharu.hideout.exception.ap.IllegalActivityPubObjectException
import dev.usbharu.hideout.service.core.Transaction
import io.ktor.http.*
import org.springframework.stereotype.Service

@Service
interface APCreateService {
    suspend fun receiveCreate(create: Create): ActivityPubResponse
}

@Service
class APCreateServiceImpl(
    private val apNoteService: APNoteService,
    private val transaction: Transaction
) : APCreateService {
    override suspend fun receiveCreate(create: Create): ActivityPubResponse {
        val value = create.`object` ?: throw IllegalActivityPubObjectException("object is null")
        if (value.type.contains("Note").not()) {
            throw IllegalActivityPubObjectException("object is not Note")
        }

        return transaction.transaction {
            val note = value as Note
            apNoteService.fetchNote(note)
            ActivityPubStringResponse(HttpStatusCode.OK, "Created")
        }
    }
}
