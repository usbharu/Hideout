package dev.usbharu.hideout.activitypub.service.activity.create

import dev.usbharu.hideout.activitypub.domain.model.Create
import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.activitypub.service.objects.note.APNoteService
import dev.usbharu.hideout.application.external.Transaction
import org.springframework.stereotype.Service

@Service
class CreateActivityProcessor(transaction: Transaction, private val apNoteService: APNoteService) :
    AbstractActivityPubProcessor<Create>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Create>) {
        apNoteService.fetchNote(activity.activity.apObject as Note)
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Create

    override fun type(): Class<Create> = Create::class.java
}
