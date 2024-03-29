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

import dev.usbharu.hideout.core.domain.model.filter.FilterType
import dev.usbharu.hideout.core.infrastructure.exposedrepository.FilterKeywords
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Filters
import dev.usbharu.hideout.core.infrastructure.exposedrepository.toFilter
import dev.usbharu.hideout.core.infrastructure.exposedrepository.toFilterKeyword
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Repository

@Repository
class ExposedFilterQueryService : FilterQueryService {
    override suspend fun findByUserIdAndType(userId: Long, types: List<FilterType>): List<FilterQueryModel> {
        return Filters
            .rightJoin(FilterKeywords)
            .selectAll()
            .where { Filters.userId eq userId }
            .toFilterQueryModel()
    }

    override suspend fun findByUserId(userId: Long): List<FilterQueryModel> {
        return Filters
            .rightJoin(FilterKeywords)
            .selectAll()
            .where { Filters.userId eq userId }
            .toFilterQueryModel()
    }

    override suspend fun findByUserIdAndId(userId: Long, id: Long): FilterQueryModel? {
        return Filters
            .leftJoin(FilterKeywords)
            .selectAll()
            .where { Filters.userId eq userId and (Filters.id eq id) }
            .toFilterQueryModel()
            .firstOrNull()
    }

    override suspend fun findByUserIdAndKeywordId(userId: Long, keywordId: Long): FilterQueryModel? {
        return Filters
            .leftJoin(FilterKeywords)
            .selectAll()
            .where { Filters.userId eq userId and (FilterKeywords.id eq keywordId) }
            .toFilterQueryModel()
            .firstOrNull()
    }

    private fun Query.toFilterQueryModel(): List<FilterQueryModel> {
        return this
            .groupBy { it[Filters.id] }
            .map { it.value }
            .map {
                FilterQueryModel.of(
                    it.first().toFilter(),
                    it.map { resultRow -> resultRow.toFilterKeyword() }
                )
            }
    }
}
