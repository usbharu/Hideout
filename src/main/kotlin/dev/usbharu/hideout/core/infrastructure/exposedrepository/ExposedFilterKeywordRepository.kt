package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.application.service.id.IdGenerateService
import dev.usbharu.hideout.core.domain.model.filter.FilterMode
import dev.usbharu.hideout.core.domain.model.filterkeyword.FilterKeyword
import dev.usbharu.hideout.core.domain.model.filterkeyword.FilterKeywordRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedFilterKeywordRepository(private val idGenerateService: IdGenerateService) : FilterKeywordRepository,
    AbstractRepository() {
    override val logger: Logger
        get() = Companion.logger

    override suspend fun generateId(): Long = idGenerateService.generateId()

    override suspend fun save(filterKeyword: FilterKeyword): FilterKeyword = query {
        val empty = FilterKeywords.selectAll().where { FilterKeywords.id eq filterKeyword.id }.empty()
        if (empty) {
            FilterKeywords.insert {
                it[id] = filterKeyword.id
                it[filterId] = filterKeyword.filterId
                it[keyword] = filterKeyword.keyword
                it[mode] = filterKeyword.mode.name
            }
        } else {
            FilterKeywords.update({ FilterKeywords.id eq filterKeyword.id }) {
                it[filterId] = filterKeyword.filterId
                it[keyword] = filterKeyword.keyword
                it[mode] = filterKeyword.mode.name
            }
        }
        filterKeyword
    }

    override suspend fun saveAll(filterKeywordList: List<FilterKeyword>): Unit = query {
        FilterKeywords.batchInsert(filterKeywordList, ignore = true) {
            this[FilterKeywords.id] = it.id
            this[FilterKeywords.filterId] = it.filterId
            this[FilterKeywords.keyword] = it.keyword
            this[FilterKeywords.mode] = it.mode.name
        }
    }

    override suspend fun findById(id: Long): FilterKeyword? = query {
        return@query FilterKeywords.selectAll().where { FilterKeywords.id eq id }.singleOrNull()?.toFilterKeyword()
    }

    override suspend fun deleteById(id: Long): Unit = query {
        FilterKeywords.deleteWhere { FilterKeywords.id eq id }
    }

    override suspend fun deleteByFilterId(filterId: Long): Unit = query {
        FilterKeywords.deleteWhere { FilterKeywords.filterId eq filterId }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedFilterKeywordRepository::class.java)
    }
}

fun ResultRow.toFilterKeyword(): FilterKeyword {
    return FilterKeyword(
        this[FilterKeywords.id],
        this[FilterKeywords.filterId],
        this[FilterKeywords.keyword],
        this[FilterKeywords.mode].let { FilterMode.valueOf(it) }
    )
}

object FilterKeywords : Table("filter_keywords") {
    val id = long("id")
    val filterId = long("filter_id").references(Filters.id)
    val keyword = varchar("keyword", 1000)
    val mode = varchar("mode", 100)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
