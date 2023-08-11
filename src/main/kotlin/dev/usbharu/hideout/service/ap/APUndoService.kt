package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ap.Undo

interface APUndoService {
    suspend fun receiveUndo(undo: Undo): ActivityPubResponse
}
