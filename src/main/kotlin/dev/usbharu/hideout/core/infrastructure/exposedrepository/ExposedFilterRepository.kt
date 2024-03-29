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

package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.domain.model.filter.Filter
import dev.usbharu.hideout.core.domain.model.filter.FilterAction
import dev.usbharu.hideout.core.domain.model.filter.FilterRepository
import dev.usbharu.hideout.core.domain.model.filter.FilterType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedFilterRepository(private val idGenerateService: IdGenerateService) : FilterRepository,
    AbstractRepository() {
    override val logger: Logger
        get() = Companion.logger

    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(filter: Filter): Filter = query {
        val empty = Filters.selectAll().where {
            Filters.id eq filter.id
        }.forUpdate().empty()
        if (empty) {
            Filters.insert {
                it[id] = filter.id
                it[userId] = filter.userId
                it[name] = filter.name
                it[context] = filter.context.joinToString(",") { filterType -> filterType.name }
                it[filterAction] = filter.filterAction.name
            }
        } else {
            Filters.update({ Filters.id eq filter.id }) {
                it[userId] = filter.userId
                it[name] = filter.name
                it[context] = filter.context.joinToString(",") { filterType -> filterType.name }
                it[filterAction] = filter.filterAction.name
            }
        }
        filter
    }

    override suspend fun findById(id: Long): Filter? = query {
        return@query Filters.selectAll().where { Filters.id eq id }.singleOrNull()?.toFilter()
    }

    override suspend fun findByUserIdAndId(userId: Long, id: Long): Filter? = query {
        return@query Filters.selectAll().where { Filters.userId eq userId and (Filters.id eq id) }.singleOrNull()
            ?.toFilter()
    }

    override suspend fun findByUserIdAndType(userId: Long, types: List<FilterType>): List<Filter> = query {
        return@query Filters.selectAll().where { Filters.userId eq userId }.map { it.toFilter() }
            .filter { it.context.containsAll(types) }
    }

    override suspend fun deleteById(id: Long): Unit = query {
        Filters.deleteWhere { Filters.id eq id }
    }

    override suspend fun deleteByUserIdAndId(userId: Long, id: Long) {
        Filters.deleteWhere { Filters.userId eq userId and (Filters.id eq id) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedFilterRepository::class.java)
    }
}

fun ResultRow.toFilter(): Filter = Filter(
    this[Filters.id],
    this[Filters.userId],
    this[Filters.name],
    this[Filters.context].split(",").filterNot(String::isEmpty).map { FilterType.valueOf(it) },
    this[Filters.filterAction].let { FilterAction.valueOf(it) }
)

object Filters : Table() {
    val id = long("id")
    val userId = long("user_id").references(Actors.id)
    val name = varchar("name", 255)
    val context = varchar("context", 500)
    val filterAction = varchar("action", 255)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
