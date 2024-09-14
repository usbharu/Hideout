package dev.usbharu.hideout.core.domain.model.filter

class FilterKeyword(
    val id: FilterKeywordId,
    var keyword: FilterKeywordKeyword,
    val mode: FilterMode
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FilterKeyword

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
