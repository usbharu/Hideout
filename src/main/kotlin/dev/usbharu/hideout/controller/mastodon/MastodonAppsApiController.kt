package dev.usbharu.hideout.controller.mastodon

import dev.usbharu.hideout.controller.mastodon.generated.AppApi
import dev.usbharu.hideout.domain.mastodon.model.generated.Application
import dev.usbharu.hideout.domain.mastodon.model.generated.AppsRequest
import dev.usbharu.hideout.service.api.mastodon.AppApiService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

@Controller
class MastodonAppsApiController(private val appApiService: AppApiService) : AppApi {
    override suspend fun apiV1AppsPost(appsRequest: AppsRequest): ResponseEntity<Application> {
        println(appsRequest)
        return ResponseEntity(
            appApiService.createApp(appsRequest),
            HttpStatus.OK
        )
    }

    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/api/v1/apps"],
        produces = ["application/json"],
        consumes = ["application/x-www-form-urlencoded"]
    )
    suspend fun apiV1AppsPost(@RequestParam map: Map<String, String>): ResponseEntity<Application> {
        val appsRequest =
            AppsRequest(map.getValue("client_name"), map.getValue("redirect_uris"), map["scopes"], map["website"])
        return ResponseEntity(
            appApiService.createApp(appsRequest),
            HttpStatus.OK
        )
    }
}
