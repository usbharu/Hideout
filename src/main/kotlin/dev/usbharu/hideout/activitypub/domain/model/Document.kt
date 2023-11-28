package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Document : Object, HasName {

    var mediaType: String? = null
    var url: String? = null
    override val name: String

    constructor(
        type: List<String> = emptyList(),
        name: String,
        mediaType: String,
        url: String
    ) : super(
        type = add(type, "Document")
    ) {
        this.mediaType = mediaType
        this.url = url
        this.name = name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Document

        if (mediaType != other.mediaType) return false
        if (url != other.url) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (mediaType?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        return result
    }

    override fun toString(): String = "Document(mediaType=$mediaType, url=$url, name='$name') ${super.toString()}"
}
