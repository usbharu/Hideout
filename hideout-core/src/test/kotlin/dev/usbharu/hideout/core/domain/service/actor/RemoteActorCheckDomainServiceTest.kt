package dev.usbharu.hideout.core.domain.service.actor

import dev.usbharu.hideout.application.config.ApplicationConfig
import org.junit.jupiter.api.Test
import java.net.URI

class RemoteActorCheckDomainServiceTest {
    @Test
    fun リモートのドメインならtrueを返す() {
        val actor =

            RemoteActorCheckDomainService(ApplicationConfig(URI.create("https://example.com").toURL())).isRemoteActor()
    }
}