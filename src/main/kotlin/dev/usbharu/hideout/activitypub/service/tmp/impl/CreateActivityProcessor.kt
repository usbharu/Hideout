package dev.usbharu.hideout.activitypub.service.tmp.impl

import dev.usbharu.hideout.activitypub.domain.model.Create
import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.activitypub.service.objects.note.APNoteService
import dev.usbharu.hideout.activitypub.service.tmp.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.tmp.ActivityPubProcessContext
import dev.usbharu.hideout.application.external.Transaction
import org.springframework.stereotype.Service

@Service
class CreateActivityProcessor(transaction: Transaction, private val apNoteService: APNoteService) :
    AbstractActivityPubProcessor<Create>(transaction, false) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Create>) {
        apNoteService.fetchNote(activity.activity.`object` as Note)
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Create

    override fun type(): Class<Create> = Create::class.java
}
