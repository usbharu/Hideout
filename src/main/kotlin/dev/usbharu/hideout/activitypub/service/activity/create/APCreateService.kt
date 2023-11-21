package dev.usbharu.hideout.activitypub.service.activity.create

import dev.usbharu.hideout.activitypub.domain.exception.IllegalActivityPubObjectException
import dev.usbharu.hideout.activitypub.domain.model.Create
import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.service.objects.note.APNoteService
import dev.usbharu.hideout.application.external.Transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

interface APCreateService {
    suspend fun receiveCreate(create: Create)
}

@Service
class APCreateServiceImpl(
    private val apNoteService: APNoteService,
    private val transaction: Transaction
) : APCreateService {
    override suspend fun receiveCreate(create: Create) {
        LOGGER.debug("START Create new remote note.")
        LOGGER.trace("{}", create)

        val value = create.`object` ?: throw IllegalActivityPubObjectException("object is null")
        if (value.type.contains("Note").not()) {
            LOGGER.warn("FAILED Object type is not 'Note'")
            throw IllegalActivityPubObjectException("object is not Note")
        }

        return transaction.transaction {
            val note = value as Note
            apNoteService.fetchNote(note)
            LOGGER.debug("SUCCESS Create new remote note. ${note.id} by ${note.attributedTo}")
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(APCreateServiceImpl::class.java)
    }
}
