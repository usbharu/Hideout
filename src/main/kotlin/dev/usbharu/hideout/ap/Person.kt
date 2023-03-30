package dev.usbharu.hideout.ap

open class Person : Object {
    private var id:String? = null
    var preferredUsername:String? = null
    var summary:String? = null
    var inbox:String? = null
    private var outbox:String? = null
    private var url:String? = null
    private var icon:Image? = null
    private var publicKey:Key? = null
    protected constructor() : super()
    constructor(
        type: List<String> = emptyList(),
        name: String,
        id: String?,
        preferredUsername: String?,
        summary: String?,
        inbox: String?,
        outbox: String?,
        url: String?,
        icon: Image?,
        publicKey: Key?
    ) : super(add(type,"Person"), name) {
        this.id = id
        this.preferredUsername = preferredUsername
        this.summary = summary
        this.inbox = inbox
        this.outbox = outbox
        this.url = url
        this.icon = icon
        this.publicKey =  publicKey
    }

}
