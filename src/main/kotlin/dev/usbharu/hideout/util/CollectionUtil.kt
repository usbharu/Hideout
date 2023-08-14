package dev.usbharu.hideout.util

class CollectionUtil

fun <T> Iterable<T>.singleOr(block: (e: RuntimeException) -> Throwable): T {
    return try {
        this.single()
    } catch (e: NoSuchElementException) {
        throw block(e)
    } catch (e: IllegalArgumentException) {
        throw block(e)
    }
}
