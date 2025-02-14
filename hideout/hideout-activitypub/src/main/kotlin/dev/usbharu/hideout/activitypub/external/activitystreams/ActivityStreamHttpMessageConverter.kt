package dev.usbharu.hideout.activitypub.external.activitystreams

import com.github.jsonldjava.core.JsonLdOptions
import com.github.jsonldjava.core.JsonLdProcessor
import com.github.jsonldjava.utils.JsonUtils
import dev.usbharu.activitystreamsserialization.json.impl.JacksonSerializationConverter
import dev.usbharu.activitystreamsserialization.other.JsonLd
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.stereotype.Component

@Component
class ActivityStreamHttpMessageConverter : HttpMessageConverter<JsonLd> {
    override fun canRead(clazz: Class<*>, mediaType: MediaType?): Boolean {
        return false
    }

    override fun canWrite(clazz: Class<*>, mediaType: MediaType?): Boolean {
        return JsonLd::class.java.isAssignableFrom(clazz)
    }

    override fun getSupportedMediaTypes(): MutableList<MediaType> {
        return mutableListOf()
    }

    override fun write(t: JsonLd, contentType: MediaType?, outputMessage: HttpOutputMessage) {
        outputMessage.headers.contentType = MediaType.APPLICATION_JSON
        outputMessage.body.bufferedWriter()
            .use {
                it.write(JsonUtils.toString(
                    JsonLdProcessor.compact(
                        JsonUtils.fromString(JacksonSerializationConverter.convert(t.json).toString()), "https://www.w3.org/ns/activitystreams",
                        JsonLdOptions()
                    )
                ))
            }
    }

    override fun read(clazz: Class<out JsonLd>, inputMessage: HttpInputMessage): JsonLd {
        TODO("Not yet implemented")
    }

}