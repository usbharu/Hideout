package dev.usbharu.hideout.core.infrastructure.springframework

import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.HideoutUserDetails
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.servlet.AsyncHandlerInterceptor
import java.util.*

@Component
class ApplicationRequestLogInterceptor : AsyncHandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        MDC.put(requestId, UUID.randomUUID().toString())
        MDC.put(Companion.handler, handler.toString())
        val userDetailId = when (val principal = SecurityContextHolder.getContext().authentication?.principal) {
            is HideoutUserDetails -> {
                principal.userDetailsId
            }

            is Jwt -> {
                principal.getClaim<String>("uid")?.toLongOrNull()
            }

            else -> {
                null
            }
        }



        if (userDetailId != null) {
            MDC.put(userId, userDetailId.toString())
        }

        logger.info("START")
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        removeMdc()
    }

    override fun afterConcurrentHandlingStarted(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ) {
        removeMdc()
    }

    private fun removeMdc() {
        MDC.remove(requestId)
        MDC.remove(userId)
        MDC.remove(handler)
    }

    companion object {
        const val requestId: String = "requestId"
        const val userId: String = "userId"
        const val handler: String = "handler"
        private val logger = LoggerFactory.getLogger(ApplicationRequestLogInterceptor::class.java)
    }
}