package dev.usbharu.hideout.domain.model.ap

open class Create : Object {
    var `object`: Object? = null

    protected constructor() : super()
    constructor(type: List<String> = emptyList(), name: String, `object`: Object?) : super(add(type, "Create"), name) {
        this.`object` = `object`
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Create) return false
        if (!super.equals(other)) return false

        return `object` == other.`object`
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (`object`?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Create(`object`=$`object`) ${super.toString()}"
    }


}
