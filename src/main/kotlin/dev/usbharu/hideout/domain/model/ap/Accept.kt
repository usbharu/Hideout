package dev.usbharu.hideout.domain.model.ap

open class Accept : Object {
    var `object`: Object? = null

    protected constructor() : super()
    constructor(
        type: List<String> = emptyList(),
        name: String,
        `object`: Object?,
        actor: String?
    ) : super(add(type, "Accept"), name, actor) {
        this.`object` = `object`
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Accept) return false
        if (!super.equals(other)) return false

        if (`object` != other.`object`) return false
        return actor == other.actor
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (`object`?.hashCode() ?: 0)
        result = 31 * result + (actor?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "Accept(`object`=$`object`, actor=$actor) ${super.toString()}"
}
