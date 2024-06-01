package dev.usbharu.hideout.core.domain.model.actor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class Actors2Test {
    @Test
    fun alsoKnownAsに自分自身が含まれてはいけない() {
        val actor = TestActor2Factory.create(publicKey = ActorPublicKey(""))

        assertThrows<IllegalArgumentException> {
            actor.alsoKnownAs = setOf(actor.id)
        }
    }

    @Test
    fun moveToに自分自身が設定されてはいけない() {
        val actor = TestActor2Factory.create(publicKey = ActorPublicKey(""))

        assertThrows<IllegalArgumentException> {
            actor.moveTo = actor.id
        }
    }


}