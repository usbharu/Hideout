package dev.usbharu.hideout.activitypub.external.activitystreams

import org.junit.jupiter.api.Test

class ActorTranslatorTest {
    @Test
    fun translate() {
        val translate = ActorTranslator().translate(TestActorFactory.create(), null, null)
        println(translate)

    }
}