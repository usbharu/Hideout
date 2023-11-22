package dev.usbharu.hideout.core.domain.model.tmp

@JvmInline
value class UserId private constructor(private val id: Long) {
    companion object {
        fun of(id: Long): UserId {
            require(id >= 0)
            return UserId(id)
        }

        val NULL = UserId(-1)
    }
}
