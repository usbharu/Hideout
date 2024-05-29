package dev.usbharu.hideout.core.domain.model.actor

import org.junit.jupiter.api.Test

class Actor2Test {
    @Test
    fun alsoKnownAsに自分自身が含まれてはいけない() {
        TestActor2Factory.create()
    }
}