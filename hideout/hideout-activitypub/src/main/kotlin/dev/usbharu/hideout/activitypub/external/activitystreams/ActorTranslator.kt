package dev.usbharu.hideout.activitypub.external.activitystreams

import dev.usbharu.activitystreamsserialization.activity.pub.ActivityPubActor
import dev.usbharu.activitystreamsserialization.dsl.JsonLdBuilder
import dev.usbharu.activitystreamsserialization.other.JsonLd
import dev.usbharu.hideout.core.domain.model.actor.Actor

class ActorTranslator {
    fun translate(actor: Actor): JsonLd {
        //todo actorにbot等の属性が生えてきたら対応する
        val person = JsonLdBuilder().Person {
            name(actor.name.name)
            id(actor.url)

        }
        person as ActivityPubActor

    }
}