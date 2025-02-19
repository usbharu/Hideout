package dev.usbharu.hideout.activitypub.interfaces.wellknown

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.net.URI

@JacksonXmlRootElement(localName = "XRD", namespace = "http://docs.oasis-open.org/ns/xri/xrd-1.0")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class XRD(
    @JacksonXmlProperty(localName = "Link", namespace = "http://docs.oasis-open.org/ns/xri/xrd-1.0")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("links")
    val links: List<Link>,
    @JacksonXmlProperty(localName = "subject")
    @JsonProperty(value = "subject")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val subject: URI? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Link(
    @JacksonXmlProperty(localName = "rel", isAttribute = true) val rel: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JacksonXmlProperty(localName = "template", isAttribute = true) val template: String?,
    @JacksonXmlProperty(localName = "type", isAttribute = true) val type: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JacksonXmlProperty(localName = "href", isAttribute = true) val href: String?,
)
