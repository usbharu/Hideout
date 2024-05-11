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

package dev.usbharu.hideout.core.query.model

import dev.usbharu.hideout.core.domain.model.filter.Filter
import dev.usbharu.hideout.core.domain.model.filter.FilterAction
import dev.usbharu.hideout.core.domain.model.filter.FilterType
import dev.usbharu.hideout.core.domain.model.filterkeyword.FilterKeyword

data class FilterQueryModel(
    val id: Long,
    val userId: Long,
    val name: String,
    val context: List<FilterType>,
    val filterAction: FilterAction,
    val keywords: List<FilterKeyword>
) {
    companion object {
        @Suppress("FunctionMinLength")
        fun of(filter: Filter, keywords: List<FilterKeyword>): FilterQueryModel = FilterQueryModel(
            id = filter.id,
            userId = filter.userId,
            name = filter.name,
            context = filter.context,
            filterAction = filter.filterAction,
            keywords = keywords
        )
    }
}
