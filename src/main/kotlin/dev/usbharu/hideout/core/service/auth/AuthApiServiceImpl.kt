package dev.usbharu.hideout.core.service.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.application.config.CaptchaConfig
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.service.user.UserCreateDto
import dev.usbharu.hideout.core.service.user.UserService
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AuthApiServiceImpl(
    private val httpClient: HttpClient,
    private val captchaConfig: CaptchaConfig,
    private val objectMapper: ObjectMapper,
    private val userService: UserService
) :
    AuthApiService {
    override suspend fun registerAccount(registerAccountDto: RegisterAccountDto): Actor {
        if (captchaConfig.reCaptchaSecretKey != null && captchaConfig.reCaptchaSiteKey != null) {
            val get =
                httpClient.get("https://www.google.com/recaptcha/api/siteverify?secret=" + captchaConfig.reCaptchaSecretKey + "&response=" + registerAccountDto.recaptchaResponse)
            val recaptchaResult = objectMapper.readValue<RecaptchaResult>(get.bodyAsText())
            logger.debug("reCAPTCHA: {}", recaptchaResult)
            require(recaptchaResult.success)
            require(!(recaptchaResult.score < 0.5))
        }


        val createLocalUser = userService.createLocalUser(
            UserCreateDto(
                registerAccountDto.username,
                registerAccountDto.username,
                "",
                registerAccountDto.password
            )
        )

        return createLocalUser
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthApiServiceImpl::class.java)
    }
}