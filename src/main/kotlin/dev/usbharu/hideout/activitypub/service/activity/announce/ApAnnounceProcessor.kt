package dev.usbharu.hideout.activitypub.service.activity.announce

import dev.usbharu.hideout.activitypub.domain.model.Announce
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.activitypub.service.objects.note.APNoteService
import dev.usbharu.hideout.application.external.Transaction
import org.springframework.stereotype.Service

@Service
class ApAnnounceProcessor(transaction: Transaction, private val apNoteService: APNoteService) :
    AbstractActivityPubProcessor<Announce>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Announce>) {
        apNoteService.fetchAnnounce(activity.activity)
    }

    override fun isSupported(activityType: ActivityType): Boolean = ActivityType.Announce == activityType

    override fun type(): Class<Announce> = Announce::class.java
}
