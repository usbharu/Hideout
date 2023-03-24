package dev.usbharu.hideout.ap

open class Image : Object {
    private var mediaType: String? = null
    private var url: String? = null

    protected constructor() : super()
    constructor(type: List<String> = emptyList(), name: String, mediaType: String?, url: String?) : super(
        type,
        name
    ) {
        this.mediaType = mediaType
        this.url = url
    }

}
