package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Image : Object {
    private var mediaType: String? = null
    private var url: String? = null

    protected constructor() : super()
    constructor(type: List<String> = emptyList(), name: String, mediaType: String?, url: String?) : super(
        add(type, "Image"),
        name
    ) {
        this.mediaType = mediaType
        this.url = url
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Image) return false
        if (!super.equals(other)) return false

        if (mediaType != other.mediaType) return false
        return url == other.url
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (mediaType?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        return result
    }
}
