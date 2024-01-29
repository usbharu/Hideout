package dev.usbharu.hideout.application.infrastructure.exposed

class PaginationList<T, ID>(list: List<T>, val next: ID?, val prev: ID?) : List<T> by list

fun <T, ID> PaginationList<T, ID>.toHttpHeader(
    nextBlock: (string: String) -> String,
    prevBlock: (string: String) -> String
): String? {
    val mutableListOf = mutableListOf<String>()
    if (next != null) {
        mutableListOf.add("<${nextBlock(nextBlock.toString())}>; rel=\"next\";")
    }
    if (prev != null) {
        mutableListOf.add("<${prevBlock(prevBlock.toString())}>; rel=\"prev\";")
    }

    if (mutableListOf.isEmpty()) {
        return null
    }

    return mutableListOf.joinToString(",")
}
