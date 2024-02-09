package dev.usbharu.hideout.core.query.model

import dev.usbharu.hideout.core.domain.model.filter.FilterType
import dev.usbharu.hideout.core.infrastructure.exposedrepository.FilterKeywords
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Filters
import dev.usbharu.hideout.core.infrastructure.exposedrepository.toFilter
import dev.usbharu.hideout.core.infrastructure.exposedrepository.toFilterKeyword
import org.jetbrains.exposed.sql.Query
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

    private fun Query.toFilterQueryModel(): List<FilterQueryModel> {
        return this
            .groupBy { it[Filters.id] }
            .map { it.value }
            .map {
                FilterQueryModel.of(
                    it.first().toFilter(),
                    it.map { it.toFilterKeyword() }
                )
            }
    }
}