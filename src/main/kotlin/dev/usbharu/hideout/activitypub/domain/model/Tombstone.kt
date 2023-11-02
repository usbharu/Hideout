package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Tombstone : Object {
    constructor(
        type: List<String> = emptyList(),
        name: String = "Tombstone",
        actor: String? = null,
        id: String
    ) : super(add(type, "Tombstone"), name, actor, id)
}
