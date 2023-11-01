package dev.usbharu.hideout.core.infrastructure.springframework.httpsignature

import dev.usbharu.httpsignature.common.HttpRequest
import dev.usbharu.httpsignature.common.PublicKey
import dev.usbharu.httpsignature.verify.HttpSignatureVerifier
import dev.usbharu.httpsignature.verify.SignatureHeaderParser
import dev.usbharu.httpsignature.verify.VerificationResult

class HttpSignatureVerifierComposite(
    private val map: Map<String, HttpSignatureVerifier>,
    private val httpSignatureHeaderParser: SignatureHeaderParser
) : HttpSignatureVerifier {
    override fun verify(httpRequest: HttpRequest, key: PublicKey): VerificationResult {
        val signature = httpSignatureHeaderParser.parse(httpRequest.headers)
        val verify = map[signature.algorithm]?.verify(httpRequest, key)
        if (verify != null) {
            return verify
        }

        throw IllegalArgumentException("Unsupported algorithm. ${signature.algorithm}")
    }
}
