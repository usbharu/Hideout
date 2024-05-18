/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.application.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.activitypub.domain.model.StringORObjectSerializer
import dev.usbharu.hideout.activitypub.domain.model.StringOrObject
import dev.usbharu.hideout.core.infrastructure.httpsignature.HttpRequestMixIn
import dev.usbharu.httpsignature.common.HttpRequest
import dev.usbharu.httpsignature.sign.HttpSignatureSigner
import dev.usbharu.httpsignature.sign.RsaSha256HttpSignatureSigner
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.format.DateTimeFormatter
import java.util.*

@Configuration
class ActivityPubConfig {

    @Bean
    @Qualifier("activitypub")
    fun objectMapper(): ObjectMapper {
        val module = SimpleModule().addSerializer(StringOrObject::class.java, StringORObjectSerializer())

        val objectMapper = jacksonObjectMapper()
            .registerModules(module)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .setDefaultSetterInfo(JsonSetter.Value.forContentNulls(Nulls.SKIP))
            .setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SKIP))
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            .configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true)
            .addMixIn(HttpRequest::class.java, HttpRequestMixIn::class.java)

        return objectMapper
    }

    @Bean
    @Qualifier("http")
    fun dateTimeFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)

    @Bean
    fun httpSignatureSigner(): HttpSignatureSigner = RsaSha256HttpSignatureSigner()
}
