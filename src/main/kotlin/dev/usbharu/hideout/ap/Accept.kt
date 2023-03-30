package dev.usbharu.hideout.ap

open class Accept : Object {
    public var `object`:Object? = null
    public var actor:String? = null
    protected constructor() : super()
    constructor(
        type: List<String> = emptyList(),
        name: String,
        `object`: Object?,
        actor: String?
    ) : super(add(type,"Accept"), name) {
        this.`object` = `object`
        this.actor = actor
    }
}
