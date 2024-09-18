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

package dev.usbharu.owl.common.task

import dev.usbharu.owl.common.property.PropertyType

/**
 * プロパティ定義
 *
 * @property map プロパティ名とプロパティタイプの[Map]
 */
class PropertyDefinition(val map: Map<String, PropertyType>) : Map<String, PropertyType> by map {
    /**
     * プロパティ定義のハッシュを求めます
     *
     * ハッシュ値はプロパティ名とプロパティタイプ名を結合したものを結合し、各文字のUTF-16コードと31を掛け続けたものです。
     *
     * @return
     */
    fun hash(): Long {
        var hash = 1L
        map.map { it.key + it.value.name }.joinToString("").map { hash *= it.code * 31 }
        return hash
    }
}
