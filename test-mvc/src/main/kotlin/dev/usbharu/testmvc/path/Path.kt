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

package dev.usbharu.testmvc.path

data class Path(
    val params: List<Parameter>,
) {
    fun buildUrls(): List<String> {
        return internalBuildUrls(params).map { "/$it" }
    }

    private fun internalBuildUrls(list: List<Parameter>): List<String> {
        if (list.size == 1) {
            return list.first().map { it.invoke() }
        }
        return list.first().map {
            it to internalBuildUrls(list.subList(1, list.size))
        }.flatMap { pair ->
            pair.second.map { pair.first.invoke() + "/" + it }
        }
    }

    constructor(vararg params: Any) : this(params.map {
        when (it) {
            is Parameter -> it
            is String -> StringParameter(it)
            is Collection<*> -> StringListParameter(it.map { it.toString() })
            is Array<*> -> StringListParameter(it.map { it.toString() })
            else -> StringParameter(it.toString())
        }
    })
}
