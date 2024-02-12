package dev.usbharu.hideout.mastodon.interfaces.api.filter

import dev.usbharu.hideout.controller.mastodon.generated.FilterApi
import dev.usbharu.hideout.core.infrastructure.springframework.security.LoginUserContextHolder
import dev.usbharu.hideout.domain.mastodon.model.generated.*
import dev.usbharu.hideout.mastodon.service.filter.MastodonFilterApiService
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
