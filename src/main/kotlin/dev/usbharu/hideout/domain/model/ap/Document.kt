package dev.usbharu.hideout.domain.model.ap

open class Document : Object {

    var mediaType: String? = null
    var url: String? = null

    protected constructor() : super()
    constructor(
        type: List<String> = emptyList(),
        name: String? = null,
        mediaType: String,
        url: String
    ) : super(
        type = add(type, "Document"),
        name = name,
        actor = null,
        id = null
    ) {
        this.mediaType = mediaType
        this.url = url
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Document) return false
        if (!super.equals(other)) return false

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

    override fun toString(): String = "Document(mediaType=$mediaType, url=$url) ${super.toString()}"
}
