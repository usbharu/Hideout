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

package dev.usbharu.hideout.mastodon.interfaces.api.filter

import dev.usbharu.hideout.controller.mastodon.generated.FilterApi
import dev.usbharu.hideout.core.infrastructure.springframework.security.LoginUserContextHolder
import dev.usbharu.hideout.domain.mastodon.model.generated.*
import dev.usbharu.hideout.mastodon.service.filter.MastodonFilterApiService
import kotlinx.coroutines.flow.Flow
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class MastodonFilterApiController(
    private val mastodonFilterApiService: MastodonFilterApiService,
    private val loginUserContextHolder: LoginUserContextHolder
) : FilterApi {

    override suspend fun apiV1FiltersIdDelete(id: String): ResponseEntity<Any> {
        mastodonFilterApiService.deleteV1FilterById(loginUserContextHolder.getLoginUserId(), id.toLong())
        return ResponseEntity.ok().build()
    }

    override suspend fun apiV1FiltersIdGet(
        id: String
    ): ResponseEntity<V1Filter> {
        return ResponseEntity.ok(
            mastodonFilterApiService.getV1FilterById(
                loginUserContextHolder.getLoginUserId(),
                id.toLong()
            )
        )
    }

    override suspend fun apiV1FiltersIdPut(
        id: String,
        phrase: String?,
        context: List<String>?,
        irreversible: Boolean?,
        wholeWord: Boolean?,
        expiresIn: Int?
    ): ResponseEntity<V1Filter> = super.apiV1FiltersIdPut(id, phrase, context, irreversible, wholeWord, expiresIn)

    override suspend fun apiV1FiltersPost(v1FilterPostRequest: V1FilterPostRequest): ResponseEntity<V1Filter> {
        return ResponseEntity.ok(
            mastodonFilterApiService.createByV1Filter(loginUserContextHolder.getLoginUserId(), v1FilterPostRequest)
        )
    }

    override suspend fun apiV2FiltersFilterIdKeywordsPost(
        filterId: String,
        filterKeywordsPostRequest: FilterKeywordsPostRequest
    ): ResponseEntity<FilterKeyword> {
        return ResponseEntity.ok(
            mastodonFilterApiService.addKeyword(
                loginUserContextHolder.getLoginUserId(),
                filterId.toLong(),
                filterKeywordsPostRequest
            )
        )
    }

    override suspend fun apiV2FiltersFilterIdStatusesPost(
        filterId: String,
        filterStatusRequest: FilterStatusRequest
    ): ResponseEntity<FilterStatus> {
        return ResponseEntity.ok(
            mastodonFilterApiService.addFilterStatus(
                loginUserContextHolder.getLoginUserId(),
                filterId.toLong(),
                filterStatusRequest
            )
        )
    }

    override fun apiV1FiltersGet(): ResponseEntity<Flow<V1Filter>> =
        ResponseEntity.ok(mastodonFilterApiService.v1Filters(loginUserContextHolder.getLoginUserId()))

    override fun apiV2FiltersFilterIdKeywordsGet(filterId: String): ResponseEntity<Flow<FilterKeyword>> {
        return ResponseEntity.ok(
            mastodonFilterApiService.filterKeywords(
                loginUserContextHolder.getLoginUserId(),
                filterId.toLong()
            )
        )
    }

    override fun apiV2FiltersFilterIdStatusesGet(filterId: String): ResponseEntity<Flow<FilterStatus>> {
        return ResponseEntity.ok(
            mastodonFilterApiService.filterStatuses(
                loginUserContextHolder.getLoginUserId(),
                filterId.toLong()
            )
        )
    }

    override fun apiV2FiltersGet(): ResponseEntity<Flow<Filter>> =
        ResponseEntity.ok(mastodonFilterApiService.filters(loginUserContextHolder.getLoginUserId()))

    override suspend fun apiV2FiltersIdDelete(id: String): ResponseEntity<Any> {
        mastodonFilterApiService.deleteById(loginUserContextHolder.getLoginUserId(), id.toLong())
        return ResponseEntity.ok().build()
    }

    override suspend fun apiV2FiltersIdGet(id: String): ResponseEntity<Filter> =
        ResponseEntity.ok(mastodonFilterApiService.getById(loginUserContextHolder.getLoginUserId(), id.toLong()))

    override suspend fun apiV2FiltersIdPut(
        id: String,
        title: String?,
        context: List<String>?,
        filterAction: String?,
        expiresIn: Int?,
        keywordsAttributes: List<FilterPubRequestKeyword>?
    ): ResponseEntity<Filter> =
        super.apiV2FiltersIdPut(id, title, context, filterAction, expiresIn, keywordsAttributes)

    override suspend fun apiV2FiltersKeywordsIdDelete(id: String): ResponseEntity<Any> {
        mastodonFilterApiService.deleteKeyword(loginUserContextHolder.getLoginUserId(), id.toLong())
        return ResponseEntity.ok().build()
    }

    override suspend fun apiV2FiltersKeywordsIdGet(id: String): ResponseEntity<FilterKeyword> {
        return ResponseEntity.ok(
            mastodonFilterApiService.getKeywordById(
                loginUserContextHolder.getLoginUserId(),
                id.toLong()
            )
        )
    }

    override suspend fun apiV2FiltersKeywordsIdPut(
        id: String,
        keyword: String?,
        wholeWord: Boolean?,
        regex: Boolean?
    ): ResponseEntity<FilterKeyword> = super.apiV2FiltersKeywordsIdPut(id, keyword, wholeWord, regex)

    override suspend fun apiV2FiltersPost(filterPostRequest: FilterPostRequest): ResponseEntity<Filter> =
        ResponseEntity.ok(
            mastodonFilterApiService.createFilter(
                loginUserContextHolder.getLoginUserId(),
                filterPostRequest
            )
        )

    override suspend fun apiV2FiltersStatusesIdDelete(id: String): ResponseEntity<Any> {
        mastodonFilterApiService.deleteFilterStatusById(loginUserContextHolder.getLoginUserId(), id.toLong())
        return ResponseEntity.ok().build()
    }

    override suspend fun apiV2FiltersStatusesIdGet(id: String): ResponseEntity<FilterStatus> {
        return ResponseEntity.ok(
            mastodonFilterApiService.getFilterStatusById(
                loginUserContextHolder.getLoginUserId(),
                id.toLong()
            )
        )
    }
}
