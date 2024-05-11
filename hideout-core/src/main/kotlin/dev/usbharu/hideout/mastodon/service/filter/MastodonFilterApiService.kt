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

package dev.usbharu.hideout.mastodon.service.filter

import dev.usbharu.hideout.core.domain.model.filter.FilterAction.hide
import dev.usbharu.hideout.core.domain.model.filter.FilterAction.warn
import dev.usbharu.hideout.core.domain.model.filter.FilterMode
import dev.usbharu.hideout.core.domain.model.filter.FilterRepository
import dev.usbharu.hideout.core.domain.model.filter.FilterType.*
import dev.usbharu.hideout.core.domain.model.filterkeyword.FilterKeywordRepository
import dev.usbharu.hideout.core.query.model.FilterQueryModel
import dev.usbharu.hideout.core.query.model.FilterQueryService
import dev.usbharu.hideout.core.service.filter.MuteService
import dev.usbharu.hideout.domain.mastodon.model.generated.*
import dev.usbharu.hideout.domain.mastodon.model.generated.FilterPostRequest.FilterAction
import dev.usbharu.hideout.domain.mastodon.model.generated.V1Filter.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Suppress("TooManyFunctions")
interface MastodonFilterApiService {
    fun v1Filters(userId: Long): Flow<V1Filter>

    suspend fun deleteV1FilterById(userId: Long, id: Long)

    suspend fun getV1FilterById(userId: Long, id: Long): V1Filter?

    suspend fun createByV1Filter(userId: Long, v1FilterRequest: V1FilterPostRequest): V1Filter

    fun filterKeywords(userId: Long, filterId: Long): Flow<FilterKeyword>

    suspend fun addKeyword(userId: Long, filterId: Long, keyword: FilterKeywordsPostRequest): FilterKeyword

    fun filterStatuses(userId: Long, filterId: Long): Flow<FilterStatus>

    suspend fun addFilterStatus(userId: Long, filterId: Long, filterStatusRequest: FilterStatusRequest): FilterStatus

    fun filters(userId: Long): Flow<Filter>

    suspend fun deleteById(userId: Long, filterId: Long)

    suspend fun getById(userId: Long, filterId: Long): Filter?

    suspend fun deleteKeyword(userId: Long, keywordId: Long)

    suspend fun getKeywordById(userId: Long, keywordId: Long): FilterKeyword?

    suspend fun createFilter(userId: Long, filterPostRequest: FilterPostRequest): Filter

    suspend fun deleteFilterStatusById(userId: Long, filterPostsId: Long)

    suspend fun getFilterStatusById(userId: Long, filterPostsId: Long): FilterStatus?
}

@Service
class MastodonFilterApiServiceImpl(
    private val muteService: MuteService,
    private val filterQueryService: FilterQueryService,
    private val filterRepository: FilterRepository,
    private val filterKeywordRepository: FilterKeywordRepository
) : MastodonFilterApiService {
    override fun v1Filters(userId: Long): Flow<V1Filter> {
        return runBlocking { filterQueryService.findByUserId(userId) }.flatMap { filterQueryModel ->
            filterQueryModel.keywords.map {
                V1Filter(
                    id = it.id.toString(),
                    phrase = it.keyword,
                    context = filterQueryModel.context.map { filterType ->
                        when (filterType) {
                            home -> Context.home
                            notifications -> Context.notifications
                            public -> Context.public
                            thread -> Context.thread
                            account -> Context.account
                        }
                    },
                    expiresAt = null,
                    irreversible = false,
                    wholeWord = (it.mode != FilterMode.WHOLE_WORD).not()
                )
            }
        }.asFlow()
    }

    override suspend fun deleteV1FilterById(userId: Long, id: Long) {
        val keywordId = filterQueryService.findByUserIdAndKeywordId(userId, id)?.keywords?.singleOrNull()?.id ?: return

        filterKeywordRepository.deleteById(keywordId)
    }

    override suspend fun getV1FilterById(userId: Long, id: Long): V1Filter? {
        val filterQueryModel = filterQueryService.findByUserIdAndKeywordId(userId, id) ?: return null

        val filterKeyword = filterQueryModel.keywords.firstOrNull() ?: return null

        return v1Filter(filterQueryModel, filterKeyword)
    }

    private fun v1Filter(
        filterQueryModel: FilterQueryModel,
        filterKeyword: dev.usbharu.hideout.core.domain.model.filterkeyword.FilterKeyword
    ) = V1Filter(
        id = filterQueryModel.id.toString(),
        phrase = filterKeyword.keyword,
        context = filterQueryModel.context.map {
            when (it) {
                home -> Context.home
                notifications -> Context.notifications
                public -> Context.public
                thread -> Context.thread
                account -> Context.account
            }
        },
        expiresAt = null,
        irreversible = false,
        wholeWord = filterKeyword.mode == FilterMode.WHOLE_WORD
    )

    override suspend fun createByV1Filter(userId: Long, v1FilterRequest: V1FilterPostRequest): V1Filter {
        val createFilter = muteService.createFilter(
            title = v1FilterRequest.phrase,
            context = v1FilterRequest.context.map {
                when (it) {
                    V1FilterPostRequest.Context.home -> home
                    V1FilterPostRequest.Context.notifications -> notifications
                    V1FilterPostRequest.Context.public -> public
                    V1FilterPostRequest.Context.thread -> thread
                    V1FilterPostRequest.Context.account -> account
                }
            },
            action = warn,
            keywords = listOf(
                dev.usbharu.hideout.core.service.filter.FilterKeyword(
                    v1FilterRequest.phrase,
                    if (v1FilterRequest.wholeWord == true) {
                        FilterMode.WHOLE_WORD
                    } else {
                        FilterMode.NONE
                    }
                )
            ),
            loginUser = userId
        )

        return v1Filter(createFilter, createFilter.keywords.first())
    }

    override fun filterKeywords(userId: Long, filterId: Long): Flow<FilterKeyword> =
        runBlocking { filterQueryService.findByUserIdAndId(userId, filterId) }
            ?.keywords
            ?.map {
                toFilterKeyword(
                    it
                )
            }
            .orEmpty()
            .asFlow()

    override suspend fun addKeyword(userId: Long, filterId: Long, keyword: FilterKeywordsPostRequest): FilterKeyword {
        val id = filterQueryService.findByUserIdAndId(userId, filterId)?.id
            ?: throw IllegalArgumentException("filter not found.")

        val filterKeyword = filterKeywordRepository.save(
            dev.usbharu.hideout.core.domain.model.filterkeyword.FilterKeyword(
                id = filterKeywordRepository.generateId(),
                filterId = id,
                keyword = keyword.keyword,
                mode = if (keyword.regex == true) {
                    FilterMode.REGEX
                } else if (keyword.wholeWord == true) {
                    FilterMode.WHOLE_WORD
                } else {
                    FilterMode.NONE
                }
            )
        )

        return toFilterKeyword(filterKeyword)
    }

    override fun filterStatuses(userId: Long, filterId: Long): Flow<FilterStatus> = emptyFlow()

    override suspend fun addFilterStatus(
        userId: Long,
        filterId: Long,
        filterStatusRequest: FilterStatusRequest
    ): FilterStatus {
        TODO()
    }

    override fun filters(userId: Long): Flow<Filter> =
        runBlocking { filterQueryService.findByUserId(userId) }.map { filterQueryModel ->
            toFilter(filterQueryModel)
        }.asFlow()

    private fun toFilter(filterQueryModel: FilterQueryModel) = Filter(
        id = filterQueryModel.id.toString(),
        title = filterQueryModel.name,
        context = filterQueryModel.context.map {
            when (it) {
                home -> Filter.Context.home
                notifications -> Filter.Context.notifications
                public -> Filter.Context.public
                thread -> Filter.Context.thread
                account -> Filter.Context.account
            }
        },
        expiresAt = null,
        filterAction = when (filterQueryModel.filterAction) {
            warn -> Filter.FilterAction.warn
            hide -> Filter.FilterAction.hide
        },
        keywords = filterQueryModel.keywords.map {
            toFilterKeyword(it)
        },
        statuses = null
    )

    private fun toFilterKeyword(it: dev.usbharu.hideout.core.domain.model.filterkeyword.FilterKeyword) = FilterKeyword(
        it.id.toString(),
        it.keyword,
        it.mode == FilterMode.WHOLE_WORD
    )

    override suspend fun deleteById(userId: Long, filterId: Long) =
        filterRepository.deleteByUserIdAndId(userId, filterId)

    override suspend fun getById(userId: Long, filterId: Long): Filter? =
        filterQueryService.findByUserIdAndId(userId, filterId)?.let { toFilter(it) }

    override suspend fun deleteKeyword(userId: Long, keywordId: Long) {
        val id = filterQueryService.findByUserIdAndKeywordId(userId, keywordId)?.keywords?.singleOrNull()?.id ?: return

        filterKeywordRepository.deleteById(id)
    }

    override suspend fun getKeywordById(userId: Long, keywordId: Long): FilterKeyword? {
        return filterQueryService
            .findByUserIdAndKeywordId(userId, keywordId)
            ?.keywords
            ?.firstOrNull()
            ?.let { toFilterKeyword(it) }
    }

    override suspend fun createFilter(userId: Long, filterPostRequest: FilterPostRequest): Filter {
        val keywords = filterPostRequest.keywordsAttributes.orEmpty().map {
            dev.usbharu.hideout.core.service.filter.FilterKeyword(
                it.keyword,
                if (it.regex == true) {
                    FilterMode.REGEX
                } else if (it.wholeWord == true) {
                    FilterMode.WHOLE_WORD
                } else {
                    FilterMode.NONE
                }
            )
        }
        return toFilter(
            muteService.createFilter(
                title = filterPostRequest.title,
                context = filterPostRequest.context.map {
                    when (it) {
                        FilterPostRequest.Context.home -> home
                        FilterPostRequest.Context.notifications -> notifications
                        FilterPostRequest.Context.public -> public
                        FilterPostRequest.Context.thread -> thread
                        FilterPostRequest.Context.account -> account
                    }
                },
                action = when (filterPostRequest.filterAction) {
                    FilterAction.warn -> warn
                    FilterAction.hide -> warn
                    null -> warn
                },
                keywords = keywords,
                loginUser = userId
            )
        )
    }

    override suspend fun deleteFilterStatusById(userId: Long, filterPostsId: Long) = Unit

    override suspend fun getFilterStatusById(userId: Long, filterPostsId: Long): FilterStatus? = null
}
