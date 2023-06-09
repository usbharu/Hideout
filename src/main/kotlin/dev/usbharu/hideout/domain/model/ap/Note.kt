package dev.usbharu.hideout.domain.model.ap

open class Note : Object {
    var attributedTo: String? = null
    var content: String? = null
    var published: String? = null
    var to: List<String> = emptyList()
    var cc: List<String> = emptyList()
    var sensitive: Boolean = false
    var inReplyTo: String? = null

    protected constructor() : super()
    constructor(
        type: List<String> = emptyList(),
        name: String,
        id: String?,
        attributedTo: String?,
        content: String?,
        published: String?,
        to: List<String> = emptyList(),
        cc: List<String> = emptyList(),
        sensitive: Boolean = false,
        inReplyTo: String? = null
    ) : super(
        type = add(type, "Note"),
        name = name,
        id = id
    ) {
        this.attributedTo = attributedTo
        this.content = content
        this.published = published
        this.to = to
        this.cc = cc
        this.sensitive = sensitive
        this.inReplyTo = inReplyTo
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Note) return false
        if (!super.equals(other)) return false

        if (id != other.id) return false
        if (attributedTo != other.attributedTo) return false
        if (content != other.content) return false
        if (published != other.published) return false
        return to == other.to
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + (attributedTo?.hashCode() ?: 0)
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + (published?.hashCode() ?: 0)
        result = 31 * result + to.hashCode()
        return result
    }

    override fun toString(): String =
        "Note(id=$id, attributedTo=$attributedTo, content=$content, published=$published, to=$to) ${super.toString()}"
}
