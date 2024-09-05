package dev.usbharu.hideout.core.application.actor

import dev.usbharu.hideout.core.domain.model.actor.Actor
import java.net.URI

data class ActorDetail(
    val id: Long,
    val name: String,
    val screenName: String,
    val host: String,
    val remoteUrl: String?,
    val locked: Boolean,
    val description: String,
    val postsCount: Int,
    val iconUrl: URI?,
    val bannerURL: URI?,
    val followingCount: Int?,
    val followersCount: Int?,
) {
    companion object {
        fun of(actor: Actor, iconUrl: URI?, bannerURL: URI?): ActorDetail {
            return ActorDetail(
                id = actor.id.id,
                name = actor.name.name,
                screenName = actor.screenName.screenName,
                host = actor.url.host,
                remoteUrl = actor.url.toString(),
                locked = actor.locked,
                description = actor.description.description,
                postsCount = actor.postsCount.postsCount,
                iconUrl = iconUrl,
                bannerURL = bannerURL,
                followingCount = actor.followingCount?.relationshipCount,
                followersCount = actor.followersCount?.relationshipCount,
            )
        }
    }
}
