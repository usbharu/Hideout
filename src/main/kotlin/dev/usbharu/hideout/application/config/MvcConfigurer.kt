package dev.usbharu.hideout.application.config

import dev.usbharu.hideout.generate.JsonOrFormModelMethodProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor

@Configuration
class MvcConfigurer(private val jsonOrFormModelMethodProcessor: JsonOrFormModelMethodProcessor) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(jsonOrFormModelMethodProcessor)
    }
}

@Configuration
class JsonOrFormModelMethodProcessorConfig {
    @Bean
    fun jsonOrFormModelMethodProcessor(converter: List<HttpMessageConverter<*>>): JsonOrFormModelMethodProcessor {
        return JsonOrFormModelMethodProcessor(
            ServletModelAttributeMethodProcessor(true),
            RequestResponseBodyMethodProcessor(
                converter
            )
        )
    }
}
