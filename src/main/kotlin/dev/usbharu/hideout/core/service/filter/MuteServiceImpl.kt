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
        name: String,
        context: List<FilterType>,
        action: FilterAction,
        keywords: List<FilterKeyword>,
        loginUser: Long
    ): Filter {
        val filter = Filter(
            filterRepository.generateId(),
            loginUser,
            name,
            context,
            action
        )

        val filterKeywordList = keywords.map {
            dev.usbharu.hideout.core.domain.model.filterkeyword.FilterKeyword(
                filterRepository.generateId(),
                filter.id,
                it.keyword,
                it.mode
            )
        }

        filterKeywordRepository.saveAll(filterKeywordList)

        return filterRepository.save(filter)
    }

    override suspend fun getFilters(userId: Long, types: List<FilterType>): List<FilterQueryModel> =
        filterQueryService.findByUserIdAndType(userId, types)

    override suspend fun deleteFilter(filterId: Long) {
        filterKeywordRepository.deleteByFilterId(filterId)
        filterRepository.deleteById(filterId)
    }
}