package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ap.Undo

interface ActivityPubUndoService {
    suspend fun receiveUndo(undo: Undo): ActivityPubResponse
}
