package dev.usbharu.hideout.core.domain.model.tmp

@JvmInline
value class InstanceId private constructor(private val id: Long) {
    companion object {
        fun pf(id: Long): InstanceId {
            require(id >= 0)
            return InstanceId(id)
        }

        val NULL = InstanceId(-1)
    }
}
