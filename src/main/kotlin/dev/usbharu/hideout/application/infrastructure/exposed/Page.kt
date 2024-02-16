package dev.usbharu.hideout.application.infrastructure.exposed

sealed class Page {
    abstract val maxId: Long?
    abstract val sinceId: Long?
    abstract val minId: Long?
    abstract val limit: Int?

    data class PageByMaxId(
        override val maxId: Long?,
        override val sinceId: Long?,
        override val limit: Int?
    ) : Page() {
        override val minId: Long? = null
    }

    data class PageByMinId(
        override val maxId: Long?,
        override val minId: Long?,
        override val limit: Int?
    ) : Page() {
        override val sinceId: Long? = null
    }

    companion object {
        @Suppress("FunctionMinLength")
        fun of(
            maxId: Long? = null,
            sinceId: Long? = null,
            minId: Long? = null,
            limit: Int? = null
        ): Page =
            if (minId != null) {
                PageByMinId(
                    maxId,
                    minId,
                    limit
                )
            } else {
                PageByMaxId(
                    maxId,
                    sinceId,
                    limit
                )
            }
    }
}
