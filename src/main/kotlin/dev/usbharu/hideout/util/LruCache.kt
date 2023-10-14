package dev.usbharu.hideout.util

import java.io.Serial

class LruCache<K, V>(private val maxSize: Int) : LinkedHashMap<K, V>(15, 0.75f, true) {

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean = size > maxSize

    companion object {
        @Serial
        private const val serialVersionUID: Long = -6446947260925053191L
    }
}
