package dev.usbharu.hideout.service.signature

import dev.usbharu.hideout.util.Base64Util
import io.ktor.http.*
import io.ktor.util.*
import org.springframework.stereotype.Component
import java.net.URL
import java.security.Signature

@Component
class HttpSignatureSignerImpl : HttpSignatureSigner {
    override suspend fun sign(
        url: String,
        method: HttpMethod,
        headers: Headers,
        requestBody: String,
        keyPair: Key,
        signHeaders: List<String>
    ): SignedRequest {
        val sign = signRaw(
            signString = buildSignString(
                url = URL(url),
                method = method,
                headers = headers,
                signHeaders = signHeaders
            ),
            keyPair = keyPair,
            signHeaders = signHeaders
        )
        val signedHeaders = headers {
            appendAll(headers)
            set("Signature", sign.signatureHeader)
        }
        return SignedRequest(
            url = url,
            method = method,
            headers = signedHeaders,
            requestBody = requestBody,
            sign = sign
        )
    }

    override suspend fun signRaw(signString: String, keyPair: Key, signHeaders: List<String>): Sign {
        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(keyPair.privateKey)
        signer.update(signString.toByteArray())
        val sign = signer.sign()
        val signature = Base64Util.encode(sign)
        return Sign(
            signature,
            """keyId="${keyPair.keyId}",algorithm="rsa-sha256",headers="${signHeaders.joinToString(" ")}",signature="$signature""""
        )
    }

    private fun buildSignString(
        url: URL,
        method: HttpMethod,
        headers: Headers,
        signHeaders: List<String>
    ): String {
        headers.toMap().map { it.key.lowercase() to it.value }.toMap()
        val result = signHeaders.joinToString("\n") {
            if (it.startsWith("(")) {
                specialHeader(it, url, method)
            } else {
                generalHeader(it, headers.get(it)!!)
            }
        }
        return result
    }

    private fun specialHeader(fieldName: String, url: URL, method: HttpMethod): String {
        if (fieldName != "(request-target)") {
            throw IllegalArgumentException(fieldName + "is unsupported type")
        }
        return "(request-target): ${method.value.lowercase()} ${url.path}"
    }

    private fun generalHeader(fieldName: String, value: String): String {
        return "$fieldName: $value"
    }
}
