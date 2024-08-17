package dev.usbharu.hideout.core.application.post

import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.media.Media
import java.net.URI

data class ActorDetail(
    val actorId: Long,
    val instanceId: Long,
    val name: String,
    val domain: String,
    val screenName: String,
    val url: URI,
    val locked: Boolean,
    val icon: URI?,
) {
    companion object {
        fun of(actor: Actor, iconMedia: Media?): ActorDetail {
            return ActorDetail(
                actor.id.id,
                actor.instance.instanceId,
                actor.name.name,
                actor.domain.domain,
                actor.screenName.screenName,
                actor.url,
                actor.locked,
                iconMedia?.url
            )
        }
    }
}
