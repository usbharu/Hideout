package dev.usbharu.hideout.activitypub.interfaces.wellknown

import dev.usbharu.hideout.activitypub.application.hostmeta.Link
import dev.usbharu.hideout.activitypub.application.hostmeta.WebHostMetadata
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/.well-known")
class HostmetaController {
    @Order(1)
    @GetMapping("/host-meta")
    fun hostmeta(): ResponseEntity<WebHostMetadata> {
        return ResponseEntity.ok().contentType(MediaType("application", "xrd+xml"))
            .body(WebHostMetadata(listOf(Link("a", "b", "c"))))
    }

    @Order(2)
    @GetMapping("/host-meta", produces = ["application/json"])
    fun hostmetaJson(): WebHostMetadata {
        return WebHostMetadata(listOf(Link("a", "b", "c")))
    }

    @GetMapping("/host-meta.json", produces = ["application/json"])
    fun hostmetaJson2(): WebHostMetadata {
        return WebHostMetadata(listOf(Link("a", "b", "c")))
    }
}