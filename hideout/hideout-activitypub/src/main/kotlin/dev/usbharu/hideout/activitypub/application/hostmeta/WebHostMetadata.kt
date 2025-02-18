package dev.usbharu.hideout.activitypub.application.hostmeta

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "XRD", namespace = "http://docs.oasis-open.org/ns/xri/xrd-1.0")
class WebHostMetadata(
    @JacksonXmlProperty(localName = "Link", namespace = "http://docs.oasis-open.org/ns/xri/xrd-1.0")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("links")
    val links: List<Link>,
)

class Link(
    @JacksonXmlProperty(localName = "rel", isAttribute = true) val rel: String,
    @JacksonXmlProperty(localName = "template", isAttribute = true) val template: String,
    @JacksonXmlProperty(localName = "type", isAttribute = true) val type: String,
)