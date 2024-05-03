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

@file:Suppress("Filename")

package dev.usbharu.hideout.core.domain.model.instance

@Suppress("ClassNaming")
class Nodeinfo2_0 {
    var metadata: Metadata? = null
    var software: Software? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Nodeinfo2_0

        if (metadata != other.metadata) return false
        if (software != other.software) return false

        return true
    }

    override fun hashCode(): Int {
        var result = metadata?.hashCode() ?: 0
        result = 31 * result + (software?.hashCode() ?: 0)
        return result
    }
}

class Metadata {
    var nodeName: String? = null
    var nodeDescription: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Metadata

        if (nodeName != other.nodeName) return false
        if (nodeDescription != other.nodeDescription) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nodeName?.hashCode() ?: 0
        result = 31 * result + (nodeDescription?.hashCode() ?: 0)
        return result
    }
}

class Software {
    var name: String? = null
    var version: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Software

        if (name != other.name) return false
        if (version != other.version) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (version?.hashCode() ?: 0)
        return result
    }
}
