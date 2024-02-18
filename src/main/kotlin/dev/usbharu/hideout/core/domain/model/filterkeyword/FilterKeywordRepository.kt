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

package dev.usbharu.hideout.core.domain.model.filterkeyword

interface FilterKeywordRepository {
    suspend fun generateId(): Long
    suspend fun save(filterKeyword: FilterKeyword): FilterKeyword
    suspend fun saveAll(filterKeywordList: List<FilterKeyword>)
    suspend fun findById(id: Long): FilterKeyword?
    suspend fun deleteById(id: Long)
    suspend fun deleteByFilterId(filterId: Long)
}
