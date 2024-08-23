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
                actorId = actor.id.id,
                instanceId = actor.instance.instanceId,
                name = actor.name.name,
                domain = actor.domain.domain,
                screenName = actor.screenName.screenName,
                url = actor.url,
                locked = actor.locked,
                icon = iconMedia?.url
            )
        }
    }
}
