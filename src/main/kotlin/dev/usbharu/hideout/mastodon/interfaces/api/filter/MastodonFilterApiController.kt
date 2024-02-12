package dev.usbharu.hideout.mastodon.interfaces.api.filter

import dev.usbharu.hideout.controller.mastodon.generated.FilterApi
import dev.usbharu.hideout.domain.mastodon.model.generated.*
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class MastodonFilterApiController : FilterApi {

    override suspend fun apiV1FiltersIdDelete(id: String): ResponseEntity<Any> {
        return super.apiV1FiltersIdDelete(id)
    }

    override suspend fun apiV1FiltersIdGet(
        id: String
    ): ResponseEntity<V1Filter> {
        return super.apiV1FiltersIdGet(id)
    }

    override suspend fun apiV1FiltersIdPut(
        id: String,
        phrase: String?,
        context: List<String>?,
        irreversible: Boolean?,
        wholeWord: Boolean?,
        expiresIn: Int?
    ): ResponseEntity<V1Filter> {
        return super.apiV1FiltersIdPut(id, phrase, context, irreversible, wholeWord, expiresIn)
    }

    override suspend fun apiV1FiltersPost(v1FilterPostRequest: V1FilterPostRequest): ResponseEntity<V1Filter> {
        return super.apiV1FiltersPost(v1FilterPostRequest)
    }

    override suspend fun apiV2FiltersFilterIdKeywordsPost(
        filterId: String,
        filterKeywordsPostRequest: FilterKeywordsPostRequest
    ): ResponseEntity<FilterKeyword> {
        return super.apiV2FiltersFilterIdKeywordsPost(filterId, filterKeywordsPostRequest)
    }

    override suspend fun apiV2FiltersFilterIdStatusesPost(
        filterId: String,
        filterStatusRequest: FilterStatusRequest
    ): ResponseEntity<FilterStatus> {
        return super.apiV2FiltersFilterIdStatusesPost(filterId, filterStatusRequest)
    }

    override suspend fun apiV2FiltersIdDelete(id: String): ResponseEntity<Any> {
        return super.apiV2FiltersIdDelete(id)
    }

    override suspend fun apiV2FiltersIdGet(id: String): ResponseEntity<Filter> {
        return super.apiV2FiltersIdGet(id)
    }

    override suspend fun apiV2FiltersIdPut(
        id: String,
        title: String?,
        context: List<String>?,
        filterAction: String?,
        expiresIn: Int?,
        keywordsAttributes: List<FilterPubRequestKeyword>?
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
        regex: Boolean?
    ): ResponseEntity<FilterKeyword> {
        return super.apiV2FiltersKeywordsIdPut(id, keyword, wholeWord, regex)
    }

    override suspend fun apiV2FiltersPost(filterPostRequest: FilterPostRequest): ResponseEntity<Filter> {
        return super.apiV2FiltersPost(filterPostRequest)
    }

    override suspend fun apiV2FiltersStatusesIdDelete(id: String): ResponseEntity<Any> {
        return super.apiV2FiltersStatusesIdDelete(id)
    }

    override suspend fun apiV2FiltersStatusesIdGet(id: String): ResponseEntity<FilterStatus> {
        return super.apiV2FiltersStatusesIdGet(id)
    }
}
