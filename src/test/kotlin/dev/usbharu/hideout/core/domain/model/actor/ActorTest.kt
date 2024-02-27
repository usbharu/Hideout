package dev.usbharu.hideout.core.domain.model.actor

import org.junit.jupiter.api.Test
import utils.UserBuilder

class ActorTest {
    @Test
    fun validator() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            UserBuilder.localUserOf(name = "うんこ")
        }
    }
}