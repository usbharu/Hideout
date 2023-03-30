package dev.usbharu.hideout.service

import dev.usbharu.hideout.domain.model.User
import dev.usbharu.hideout.domain.model.UserEntity
import dev.usbharu.hideout.util.HttpUtil
import dev.usbharu.hideout.webfinger.WebFinger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*

class WebFingerService(
    private val httpClient: HttpClient,
    private val userService: UserService,
    private val userAuthService: IUserAuthService,
    private val activityPubUserService: ActivityPubUserService
) : IWebFingerService {
    override suspend fun fetch(acct: String): WebFinger? {

        val fullName = acct.substringAfter("acct:")
        val domain = fullName.substringAfterLast("@")

        return try {
            httpClient.get("https://$domain/.well-known/webfinger?resource=acct:$fullName")
                .body<WebFinger>()
        } catch (e: ResponseException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                return null
            }
            throw e
        }
    }

    override suspend fun sync(webFinger: WebFinger): UserEntity {

        val link = webFinger.links.find {
            it.rel == "self" && HttpUtil.isContentTypeOfActivityPub(
                ContentType.parse(
                    it.type.orEmpty()
                )
            )
        }?.href ?: throw Exception()

        val fullName = webFinger.subject.substringAfter("acct:")
        val domain = fullName.substringAfterLast("@")
        val userName = fullName.substringBeforeLast("@")

        val userModel = activityPubUserService.fetchUserModel(link) ?: throw Exception()

        val user = User(
            userModel.preferredUsername ?: throw IllegalStateException(),
            domain,
            userName,
            userModel.summary.orEmpty()
        )
        return userService.create(user)
    }
}
