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

package dev.usbharu.hideout.core.domain.model.instance

class Nodeinfo private constructor() {

    var links: List<Links> = emptyList()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Nodeinfo

        return links == other.links
    }

    override fun hashCode(): Int = links.hashCode()
}

class Links private constructor() {
    var rel: String? = null
    var href: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Links

        if (rel != other.rel) return false
        if (href != other.href) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rel?.hashCode() ?: 0
        result = 31 * result + (href?.hashCode() ?: 0)
        return result
    }
}
