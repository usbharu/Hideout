package dev.usbharu.hideout.core.domain.model.tmp

@JvmInline
value class PostId private constructor(private val value: Long) {
    companion object {
        fun of(id: Long): PostId {
            require(id >= 0)
            return PostId(id)
        }

        val NULL = PostId(-1)
    }
}
