package dev.usbharu.hideout.activitypub.external.activitystreams

import dev.usbharu.activitystreamsserialization.dsl.ActivityBuilder
import dev.usbharu.activitystreamsserialization.other.JsonLd
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.media.Media
import org.springframework.stereotype.Service

@Service
class ActorTranslator {
    fun translate(actor: Actor, iconMedia: Media?, bannerMedia: Media?): JsonLd {
        // todo actorにbot等の属性が生えてきたら対応する
        val person = ActivityBuilder().Person {
            name(actor.name.name)
            id(actor.url)
            preferredUsername(actor.name.name)
            inbox(actor.inbox)
            outbox(actor.outbox)
            followers(actor.followersEndpoint)
            following(actor.followingEndpoint)
            publicKey {
                listOf(
                    Key {
                        owner(actor.url)
                        publicKeyPem(actor.publicKey.publicKey)
                        id(actor.keyId.keyId)
                    }
                )
            }
            iconMedia?.let {
                icon {
                    listOf(
                        Image {
                            url(iconMedia.url)
                        }
                    )
                }
            }
            bannerMedia?.let {
                image {
                    listOf(
                        Image {
                            url(bannerMedia.url)
                        }
                    )
                }
            }
        }
        return person
    }
}
