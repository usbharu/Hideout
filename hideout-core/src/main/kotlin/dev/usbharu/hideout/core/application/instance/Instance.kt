package dev.usbharu.hideout.core.application.instance

import dev.usbharu.hideout.core.domain.model.instance.Instance
import java.net.URI

data class Instance(val id: Long, val name: String, val url: URI, val description: String) {
    companion object {
        fun of(instance: Instance): dev.usbharu.hideout.core.application.instance.Instance {
            return Instance(
                instance.id.instanceId,
                instance.name.name,
                instance.url,
                instance.description.description
            )
        }
    }
}
