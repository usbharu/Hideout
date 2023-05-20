package dev.usbharu.hideout.domain.model.ap

open class Follow : Object {
    var `object`: String? = null

    protected constructor() : super()
    constructor(
        type: List<String> = emptyList(),
        name: String,
        `object`: String?,
        actor: String?
    ) : super(
        type = add(type, "Follow"),
        name = name,
        actor = actor
    ) {
        this.`object` = `object`
    }
}
