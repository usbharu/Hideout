package dev.usbharu.hideout.ap

open class Key : Object{
    private var id:String? = null
    private var owner:String? = null
    private var publicKeyPem:String? = null
    protected constructor() : super()
    constructor(
        type: List<String>,
        name: String,
        id: String?,
        owner: String?,
        publicKeyPem: String?
    ) : super(add(type,"Key"), name) {
        this.id = id
        this.owner = owner
        this.publicKeyPem = publicKeyPem
    }


}
