package dev.usbharu.hideout.core.application.model

import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.media.Media
import java.net.URI

data class ActorDetail(
    val id: Long,
    val name: String,
    val screenName: String,
    val host: String,
    val instanceId: Long,
    val remoteUrl: URI?,
    val locked: Boolean,
    val description: String,
    val postsCount: Int,
    val iconUrl: URI?,
    val bannerURL: URI?,
    val followingCount: Int?,
    val followersCount: Int?,
) {
    companion object {
        fun of(actor: Actor, iconMedia: Media?, bannerMedia: Media?): ActorDetail {
            return ActorDetail(
                id = actor.id.id,
                name = actor.name.name,
                screenName = actor.screenName.screenName,
                host = actor.url.host,
                instanceId = actor.instance.instanceId,
                remoteUrl = actor.url,
                locked = actor.locked,
                description = actor.description.description,
                postsCount = actor.postsCount.postsCount,
                iconUrl = iconMedia?.url,
                bannerURL = bannerMedia?.url,
                followingCount = actor.followingCount?.relationshipCount,
                followersCount = actor.followersCount?.relationshipCount,
            )
        }
    }
}
