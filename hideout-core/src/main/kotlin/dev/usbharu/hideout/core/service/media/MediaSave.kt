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

package dev.usbharu.hideout.core.service.media

data class MediaSave(
    val name: String,
    val prefix: String,
    val fileInputStream: ByteArray,
    val thumbnailInputStream: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaSave

        if (name != other.name) return false
        if (prefix != other.prefix) return false
        if (!fileInputStream.contentEquals(other.fileInputStream)) return false
        if (thumbnailInputStream != null) {
            if (other.thumbnailInputStream == null) return false
            if (!thumbnailInputStream.contentEquals(other.thumbnailInputStream)) return false
        } else if (other.thumbnailInputStream != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + prefix.hashCode()
        result = 31 * result + fileInputStream.contentHashCode()
        result = 31 * result + (thumbnailInputStream?.contentHashCode() ?: 0)
        return result
    }
}
