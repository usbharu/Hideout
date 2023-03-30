package dev.usbharu.hideout.ap

open class Follow : Object{
    public var `object`:String? = null
    public var actor:String? = null
    protected constructor() : super()
    constructor(
        type: List<String> = emptyList(),
        name: String,
        `object`: String?,
        actor: String?
    ) : super(add(type,"Follow"), name) {
        this.`object` = `object`
        this.actor = actor
    }
}
