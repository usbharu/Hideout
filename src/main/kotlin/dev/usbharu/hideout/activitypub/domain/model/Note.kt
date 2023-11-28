package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Note
@Suppress("LongParameterList")
constructor(
    type: List<String> = emptyList(),
    override val id: String,
    val attributedTo: String,
    val content: String,
    val published: String,
    val to: List<String> = emptyList(),
    val cc: List<String> = emptyList(),
    val sensitive: Boolean = false,
    val inReplyTo: String? = null,
    val attachment: List<Document> = emptyList()
) : Object(
    type = add(type, "Note")
),
    HasId {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Note

        if (id != other.id) return false
        if (attributedTo != other.attributedTo) return false
        if (content != other.content) return false
        if (published != other.published) return false
        if (to != other.to) return false
        if (cc != other.cc) return false
        if (sensitive != other.sensitive) return false
        if (inReplyTo != other.inReplyTo) return false
        if (attachment != other.attachment) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + attributedTo.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + published.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + cc.hashCode()
        result = 31 * result + sensitive.hashCode()
        result = 31 * result + (inReplyTo?.hashCode() ?: 0)
        result = 31 * result + attachment.hashCode()
        return result
    }

    override fun toString(): String {
        return "Note(" +
            "id='$id', " +
            "attributedTo='$attributedTo', " +
            "content='$content', " +
            "published='$published', " +
            "to=$to, " +
            "cc=$cc, " +
            "sensitive=$sensitive, " +
            "inReplyTo=$inReplyTo, " +
            "attachment=$attachment" +
            ")" +
            " ${super.toString()}"
    }
}
