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

package dev.usbharu.hideout.mastodon.interfaces.api

import dev.usbharu.hideout.core.application.filter.*
import dev.usbharu.hideout.core.domain.model.filter.FilterAction
import dev.usbharu.hideout.core.domain.model.filter.FilterContext
import dev.usbharu.hideout.core.domain.model.filter.FilterMode
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.Oauth2CommandExecutorFactory
import dev.usbharu.hideout.mastodon.application.filter.DeleteFilterV1
import dev.usbharu.hideout.mastodon.application.filter.DeleteFilterV1ApplicationService
import dev.usbharu.hideout.mastodon.application.filter.GetFilterV1
import dev.usbharu.hideout.mastodon.application.filter.GetFilterV1ApplicationService
import dev.usbharu.hideout.mastodon.interfaces.api.generated.FilterApi
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.*
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Filter
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.FilterKeyword
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.FilterPostRequest.Context
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.V1FilterPostRequest.Context.*
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class SpringFilterApi(
    private val oauth2CommandExecutorFactory: Oauth2CommandExecutorFactory,
    private val userRegisterFilterApplicationService: UserRegisterFilterApplicationService,
    private val getFilterV1ApplicationService: GetFilterV1ApplicationService,
    private val deleteFilterV1ApplicationService: DeleteFilterV1ApplicationService,
    private val userDeleteFilterApplicationService: UserDeleteFilterApplicationService,
    private val userGetFilterApplicationService: UserGetFilterApplicationService,
) : FilterApi {

    override suspend fun apiV1FiltersIdDelete(id: String): ResponseEntity<Any> {
        return ResponseEntity.ok(
            deleteFilterV1ApplicationService.execute(
                DeleteFilterV1(id.toLong()),
                oauth2CommandExecutorFactory.getCommandExecutor()
            )
        )
    }

    override suspend fun apiV1FiltersIdGet(id: String): ResponseEntity<V1Filter> {
        return ResponseEntity.ok(
            getFilterV1ApplicationService.execute(
                GetFilterV1(id.toLong()),
                oauth2CommandExecutorFactory.getCommandExecutor()
            )
        )
    }

    override suspend fun apiV1FiltersIdPut(
        id: String,
        phrase: String?,
        context: List<String>?,
        irreversible: Boolean?,
        wholeWord: Boolean?,
        expiresIn: Int?,
    ): ResponseEntity<V1Filter> {
        return super.apiV1FiltersIdPut(id, phrase, context, irreversible, wholeWord, expiresIn)
    }

    override suspend fun apiV1FiltersPost(v1FilterPostRequest: V1FilterPostRequest): ResponseEntity<V1Filter> {
        val executor = oauth2CommandExecutorFactory.getCommandExecutor()
        val filterMode = if (v1FilterPostRequest.wholeWord == true) {
            FilterMode.WHOLE_WORD
        } else {
            FilterMode.NONE
        }
        val filterContext = v1FilterPostRequest.context.map {
            when (it) {
                home -> FilterContext.HOME
                notifications -> FilterContext.NOTIFICATION
                public -> FilterContext.PUBLIC
                thread -> FilterContext.THREAD
                account -> FilterContext.ACCOUNT
            }
        }.toSet()
        val filter = userRegisterFilterApplicationService.execute(
            RegisterFilter(
                v1FilterPostRequest.phrase, filterContext, FilterAction.WARN,
                setOf(RegisterFilterKeyword(v1FilterPostRequest.phrase, filterMode))
            ), executor
        )
        return ResponseEntity.ok(
            getFilterV1ApplicationService.execute(
                GetFilterV1(filter.filterKeywords.first().id),
                executor
            )
        )
    }

    override suspend fun apiV2FiltersFilterIdKeywordsPost(
        filterId: String,
        filterKeywordsPostRequest: FilterKeywordsPostRequest,
    ): ResponseEntity<FilterKeyword> {
        return super.apiV2FiltersFilterIdKeywordsPost(filterId, filterKeywordsPostRequest)
    }

    override suspend fun apiV2FiltersFilterIdStatusesPost(
        filterId: String,
        filterStatusRequest: FilterStatusRequest,
    ): ResponseEntity<FilterStatus> {
        return super.apiV2FiltersFilterIdStatusesPost(filterId, filterStatusRequest)
    }

    override suspend fun apiV2FiltersIdDelete(id: String): ResponseEntity<Any> {
        userDeleteFilterApplicationService.execute(
            DeleteFilter(id.toLong()),
            oauth2CommandExecutorFactory.getCommandExecutor()
        )
        return ResponseEntity.ok(Unit)
    }

    override suspend fun apiV2FiltersIdGet(id: String): ResponseEntity<Filter> {
        val filter = userGetFilterApplicationService.execute(
            GetFilter(id.toLong()),
            oauth2CommandExecutorFactory.getCommandExecutor()
        )
        return ResponseEntity.ok(
            filter(filter)
        )
    }

    private fun filter(filter: dev.usbharu.hideout.core.application.filter.Filter) = Filter(
        id = filter.filterId.toString(),
        title = filter.name,
        context = filter.filterContext.map {
            when (it) {
                FilterContext.HOME -> Filter.Context.home
                FilterContext.NOTIFICATION -> Filter.Context.notifications
                FilterContext.PUBLIC -> Filter.Context.public
                FilterContext.THREAD -> Filter.Context.thread
                FilterContext.ACCOUNT -> Filter.Context.account
            }
        },
        expiresAt = null,
        filterAction = when (filter.filterAction) {
            FilterAction.WARN -> Filter.FilterAction.warn
            FilterAction.HIDE -> Filter.FilterAction.hide

        },
        keywords = filter.filterKeywords.map {
            FilterKeyword(
                it.id.toString(),
                it.keyword,
                it.filterMode == FilterMode.WHOLE_WORD
            )
        }, statuses = null
    )

    override suspend fun apiV2FiltersIdPut(
        id: String,
        title: String?,
        context: List<String>?,
        filterAction: String?,
        expiresIn: Int?,
        keywordsAttributes: List<FilterPubRequestKeyword>?,
    ): ResponseEntity<Filter> {
        return super.apiV2FiltersIdPut(id, title, context, filterAction, expiresIn, keywordsAttributes)
    }

    override suspend fun apiV2FiltersKeywordsIdDelete(id: String): ResponseEntity<Any> {
        return super.apiV2FiltersKeywordsIdDelete(id)
    }

    override suspend fun apiV2FiltersKeywordsIdGet(id: String): ResponseEntity<FilterKeyword> {
        return super.apiV2FiltersKeywordsIdGet(id)
    }

    override suspend fun apiV2FiltersKeywordsIdPut(
        id: String,
        keyword: String?,
        wholeWord: Boolean?,
        regex: Boolean?,
    ): ResponseEntity<FilterKeyword> {
        return super.apiV2FiltersKeywordsIdPut(id, keyword, wholeWord, regex)
    }

    override suspend fun apiV2FiltersPost(filterPostRequest: FilterPostRequest): ResponseEntity<Filter> {
        val executor = oauth2CommandExecutorFactory.getCommandExecutor()
        val filter = userRegisterFilterApplicationService.execute(
            RegisterFilter(
                filterName = filterPostRequest.title,
                filterContext = filterPostRequest.context.map {
                    when (it) {
                        Context.home -> FilterContext.HOME
                        Context.notifications -> FilterContext.NOTIFICATION
                        Context.public -> FilterContext.PUBLIC
                        Context.thread -> FilterContext.THREAD
                        Context.account -> FilterContext.ACCOUNT
                    }
                }.toSet(),
                filterAction = when (filterPostRequest.filterAction) {
                    FilterPostRequest.FilterAction.warn -> FilterAction.WARN
                    FilterPostRequest.FilterAction.hide -> FilterAction.HIDE
                    null -> FilterAction.WARN
                },
                filterKeywords = filterPostRequest.keywordsAttributes.orEmpty().map {
                    RegisterFilterKeyword(
                        it.keyword,
                        if (it.regex == true) {
                            FilterMode.REGEX
                        } else if (it.wholeWord == true) {
                            FilterMode.WHOLE_WORD
                        } else {
                            FilterMode.NONE
                        }
                    )
                }.toSet()
            ), executor
        )
        return ResponseEntity.ok(filter(filter))
    }

    override suspend fun apiV2FiltersStatusesIdDelete(id: String): ResponseEntity<Any> {
        return ResponseEntity.notFound().build()
    }

    override suspend fun apiV2FiltersStatusesIdGet(id: String): ResponseEntity<FilterStatus> {
        return ResponseEntity.notFound().build()
    }
}