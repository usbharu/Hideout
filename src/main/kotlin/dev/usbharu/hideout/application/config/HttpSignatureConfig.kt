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

import dev.usbharu.httpsignature.sign.RsaSha256HttpSignatureSigner
import dev.usbharu.httpsignature.verify.DefaultSignatureHeaderParser
import dev.usbharu.httpsignature.verify.RsaSha256HttpSignatureVerifier
import dev.usbharu.httpsignature.verify.SignatureHeaderParser
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HttpSignatureConfig {
    @Bean
    fun defaultSignatureHeaderParser(): DefaultSignatureHeaderParser = DefaultSignatureHeaderParser()

    @Bean
    fun rsaSha256HttpSignatureVerifier(
        signatureHeaderParser: SignatureHeaderParser,
        signatureSigner: RsaSha256HttpSignatureSigner
    ): RsaSha256HttpSignatureVerifier = RsaSha256HttpSignatureVerifier(signatureHeaderParser, signatureSigner)
}
