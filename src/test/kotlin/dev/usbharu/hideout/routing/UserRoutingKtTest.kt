package dev.usbharu.hideout.routing

import org.junit.jupiter.api.Test

class UserRoutingKtTest {
    @Test
    fun userIconTest() {
        println(String.Companion::class.java.classLoader)
        println(String::class.java.classLoader)
        println(String.javaClass.classLoader.getResourceAsStream("icon.png")?.readAllBytes())
    }
}
