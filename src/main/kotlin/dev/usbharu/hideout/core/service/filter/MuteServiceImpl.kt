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

package dev.usbharu.hideout.core.service.filter

import dev.usbharu.hideout.core.domain.model.filter.Filter
import dev.usbharu.hideout.core.domain.model.filter.FilterAction
import dev.usbharu.hideout.core.domain.model.filter.FilterRepository
import dev.usbharu.hideout.core.domain.model.filter.FilterType
import dev.usbharu.hideout.core.domain.model.filterkeyword.FilterKeywordRepository
import dev.usbharu.hideout.core.query.model.FilterQueryModel
import dev.usbharu.hideout.core.query.model.FilterQueryService
import org.springframework.stereotype.Service

@Service
class MuteServiceImpl(
    private val filterRepository: FilterRepository,
    private val filterKeywordRepository: FilterKeywordRepository,
    private val filterQueryService: FilterQueryService
) : MuteService {
    override suspend fun createFilter(
        title: String,
        context: List<FilterType>,
        action: FilterAction,
        keywords: List<FilterKeyword>,
        loginUser: Long
    ): FilterQueryModel {
        val filter = Filter(
            filterRepository.generateId(),
            loginUser,
            title,
            context,
            action
        )

        val filterKeywordList = keywords.map {
            dev.usbharu.hideout.core.domain.model.filterkeyword.FilterKeyword(
                filterKeywordRepository.generateId(),
                filter.id,
                it.keyword,
                it.mode
            )
        }

        val savedFilter = filterRepository.save(filter)

        filterKeywordRepository.saveAll(filterKeywordList)
        return FilterQueryModel.of(savedFilter, filterKeywordList)
    }

    override suspend fun getFilters(userId: Long, types: List<FilterType>): List<FilterQueryModel> =
        filterQueryService.findByUserIdAndType(userId, types)
}
