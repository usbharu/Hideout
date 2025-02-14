package dev.usbharu.hideout.activitypub.config

import dev.usbharu.hideout.activitypub.external.activitystreams.ActivityStreamHttpMessageConverter
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@Order(2)
class ActivityPubWebMvcConfigurer(private val activityStreamsHttpMessageConverter: ActivityStreamHttpMessageConverter) :
    WebMvcConfigurer {
    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.add(activityStreamsHttpMessageConverter)
    }
}
