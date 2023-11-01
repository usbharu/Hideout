package dev.usbharu.hideout.activitypub.domain.model.`object`

open class ObjectValue : Object {

    var `object`: String? = null

    protected constructor() : super()
    constructor(type: List<String>, name: String?, actor: String?, id: String?, `object`: String?) : super(
        type,
        name,
        actor,
        id
    ) {
        this.`object` = `object`
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ObjectValue) return false
        if (!super.equals(other)) return false

        return `object` == other.`object`
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (`object`?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "ObjectValue(`object`=$`object`) ${super.toString()}"
}
