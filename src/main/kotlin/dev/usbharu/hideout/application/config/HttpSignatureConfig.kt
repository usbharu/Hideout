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
