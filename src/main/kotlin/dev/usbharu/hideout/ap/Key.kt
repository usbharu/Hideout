package dev.usbharu.hideout.ap

open class Key : Object{
    var id:String? = null
    var owner:String? = null
    var publicKeyPem:String? = null
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Key) return false
        if (!super.equals(other)) return false

        if (id != other.id) return false
        if (owner != other.owner) return false
        return publicKeyPem == other.publicKeyPem
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + (owner?.hashCode() ?: 0)
        result = 31 * result + (publicKeyPem?.hashCode() ?: 0)
        return result
    }


}
