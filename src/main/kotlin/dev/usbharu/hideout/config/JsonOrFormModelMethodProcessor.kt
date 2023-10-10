package dev.usbharu.hideout.config

import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.annotation.ModelAttributeMethodProcessor
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor

class JsonOrFormModelMethodProcessor(
    private val modelAttributeMethodProcessor: ModelAttributeMethodProcessor,
    private val requestResponseBodyMethodProcessor: RequestResponseBodyMethodProcessor
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(JsonOrFormBind::class.java)
    }

    private val isJsonRegex = Regex("application/((\\w*)\\+)?json")

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val contentType = webRequest.getHeader("Content-Type").orEmpty()
        logger.trace("ContentType is {}", contentType)
        if (contentType.contains(isJsonRegex)) {
            logger.trace("Determine content type as json.")
            return requestResponseBodyMethodProcessor.resolveArgument(
                parameter,
                mavContainer,
                webRequest,
                binderFactory
            )
        }

        return try {
            modelAttributeMethodProcessor.resolveArgument(parameter, mavContainer, webRequest, binderFactory)
        } catch (e: Exception) {
            try {
                requestResponseBodyMethodProcessor.resolveArgument(parameter, mavContainer, webRequest, binderFactory)
            } catch (e: Exception) {
                logger.warn("Failed to bind request", e)
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(JsonOrFormModelMethodProcessor::class.java)
    }
}
