package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Image : Object {
    private var mediaType: String? = null
    private var url: String? = null

    protected constructor() : super()
    constructor(type: List<String> = emptyList(), mediaType: String?, url: String?) : super(
        add(type, "Image")
    ) {
        this.mediaType = mediaType
        this.url = url
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Image

        if (mediaType != other.mediaType) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (mediaType?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "Image(mediaType=$mediaType, url=$url) ${super.toString()}"
}
