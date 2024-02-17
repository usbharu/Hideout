/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.util

import java.io.Serial

class LruCache<K, V>(private val maxSize: Int) : LinkedHashMap<K, V>(15, 0.75f, true) {

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean = size > maxSize
    override fun toString(): String {
        return "LruCache(" +
            "maxSize=$maxSize" +
            ")" +
            " ${super.toString()}"
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -6446947260925053191L
    }
}
