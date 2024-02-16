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
