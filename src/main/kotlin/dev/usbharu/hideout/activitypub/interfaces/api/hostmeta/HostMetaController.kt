package dev.usbharu.hideout.activitypub.interfaces.api.hostmeta

import dev.usbharu.hideout.application.config.ApplicationConfig
import org.intellij.lang.annotations.Language
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HostMetaController(private val applicationConfig: ApplicationConfig) {

    val xml = //language=XML
        """<?xml version="1.0" encoding="UTF-8"?>
<XRD xmlns="http://docs.oasis-open.org/ns/xri/xrd-1.0">
    <Link rel="lrdd" type="application/xrd+xml"
          template="${applicationConfig.url}/.well-known/webfinger?resource={uri}"/>
</XRD>"""

    @Language("JSON")
    val json = """{
  "links": [
    {
      "rel": "lrdd",
      "type": "application/jrd+json",
      "template": "${applicationConfig.url}/.well-known/webfinger?resource={uri}"
    }
  ]
}"""

    @GetMapping("/.well-known/host-meta", produces = ["application/xml"])
    fun hostmeta(): ResponseEntity<String> = ResponseEntity(xml, HttpStatus.OK)

    @GetMapping("/.well-known/host-meta.json", produces = ["application/json"])
    fun hostmetJson(): ResponseEntity<String> = ResponseEntity(json, HttpStatus.OK)
}
