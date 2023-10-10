package dev.usbharu.hideout.domain.model.ap

open class Note : Object {
    var attributedTo: String? = null
    var attachment: List<Document> = emptyList()
    var content: String? = null
    var published: String? = null
    var to: List<String> = emptyList()
    var cc: List<String> = emptyList()
    var sensitive: Boolean = false
    var inReplyTo: String? = null

    protected constructor() : super()

    @Suppress("LongParameterList")
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
        inReplyTo: String? = null,
        attachment: List<Document> = emptyList()
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
        this.attachment = attachment
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Note) return false
        if (!super.equals(other)) return false

        if (attributedTo != other.attributedTo) return false
        if (attachment != other.attachment) return false
        if (content != other.content) return false
        if (published != other.published) return false
        if (to != other.to) return false
        if (cc != other.cc) return false
        if (sensitive != other.sensitive) return false
        if (inReplyTo != other.inReplyTo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (attributedTo?.hashCode() ?: 0)
        result = 31 * result + attachment.hashCode()
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + (published?.hashCode() ?: 0)
        result = 31 * result + to.hashCode()
        result = 31 * result + cc.hashCode()
        result = 31 * result + sensitive.hashCode()
        result = 31 * result + (inReplyTo?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Note(attributedTo=$attributedTo, attachment=$attachment, content=$content, published=$published, to=$to, cc=$cc, sensitive=$sensitive, inReplyTo=$inReplyTo) ${super.toString()}"
    }


}
