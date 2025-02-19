package dev.usbharu.hideout.activitypub.interfaces.wellknown

import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/.well-known")
class HostmetaController(private val linkList: List<Link> = emptyList()) {
    @Order(1)
    @GetMapping("/host-meta")
    fun hostmeta(): ResponseEntity<XRD> {
        return ResponseEntity.ok().contentType(MediaType("application", "xrd+xml"))
            .body(XRD(linkList))
    }

    @Order(2)
    @GetMapping("/host-meta", produces = ["application/json"])
    fun hostmetaJson(): XRD {
        return XRD(linkList)
    }

    @GetMapping("/host-meta.json", produces = ["application/json"])
    fun hostmetaJson2(): XRD {
        return XRD(linkList)
    }
}
