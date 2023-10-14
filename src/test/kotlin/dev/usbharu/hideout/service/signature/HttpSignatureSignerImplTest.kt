package dev.usbharu.hideout.service.signature

import dev.usbharu.hideout.util.Base64Util
import dev.usbharu.hideout.util.RsaUtil
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tech.barbero.http.message.signing.HttpMessage
import tech.barbero.http.message.signing.HttpRequest
import tech.barbero.http.message.signing.KeyMap
import tech.barbero.http.message.signing.SignatureHeaderVerifier
import java.net.URI
import java.net.URL
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.crypto.SecretKey
import kotlin.test.assertFalse

class HttpSignatureSignerImplTest {
    @Test
    fun `HTTP Signatureの署名を作成できる`() = runTest {

        val publicKey = RsaUtil.decodeRsaPublicKey(
            """MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv6tEMdAw9xk3Pt5YMxJ2t+1QZeb9p+PKpS1lVbkL5oWj6aL2Q3nRVQQabcILOb5YNUpWQVQWRjW4jkrBDuiAgvlmu126OPs4E1cVVWEqylJ5VOkOIeXpldOu/SvHM/sHPNHXYlovaHDIqT+3zp2xUmXQx2kum0b/o8Vp+wh45iIoflb62/0dQ5YZyZEp283XKne+u813BzCOa1IAsywbUvX9kUv1SaUDn3oxnjdjWgSqsJcJVU1lyiN0OrpnEg5TMVjDqN3vimoR4uqNn5Zm8rrif/o8w+/FlnWticbty5MQun0gFaCfLsR8ODm1/0DwT6WI/bRpy6zye1n4iQn/nwIDAQAB"""
        )
        val privateKey = RsaUtil.decodeRsaPrivateKey(
            """MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC/q0Qx0DD3GTc+3lgzEna37VBl5v2n48qlLWVVuQvmhaPpovZDedFVBBptwgs5vlg1SlZBVBZGNbiOSsEO6ICC+Wa7Xbo4+zgTVxVVYSrKUnlU6Q4h5emV0679K8cz+wc80ddiWi9ocMipP7fOnbFSZdDHaS6bRv+jxWn7CHjmIih+Vvrb/R1DlhnJkSnbzdcqd767zXcHMI5rUgCzLBtS9f2RS/VJpQOfejGeN2NaBKqwlwlVTWXKI3Q6umcSDlMxWMOo3e+KahHi6o2flmbyuuJ/+jzD78WWda2Jxu3LkxC6fSAVoJ8uxHw4ObX/QPBPpYj9tGnLrPJ7WfiJCf+fAgMBAAECggEAIkL4LrtbdWAxivBt7bs4M4qdW4nd/9vtRneF7LvmT6/F7CawRMGK1Nql6sbMAOdwlx4Rqx3f2W8S7YSZXBPdnQv9/DI17qehj3t6mceDwaTagX4jg5W4moq7dhAUTMtrsMiF6tPaM54tkGuObMWtg+AlYPABX8piOiE436HVErXrOaWsrQ6ReoHodTyibfO8aByzLkIb2k3nt1j8HotjjFe6ZqFVkXiGVWOUwdLpsqE+8BV6g1IF480SyKF4HnUfr/AxDnpKtTFspGCKu/w7BA6yOaaONeal0/EUA8vlfLsKdaRY2TRmCFCQzUwluBTr6ssjQyilJzgJ6VbDFpVSSQKBgQDgpt5kB7TDXN5ucD0alN0umI/rLD5TTg0rbpLo2wzfh2IAPYSiCgNOVr1Mi6JRxqSLa4KeEOCYATLu9wrFU8y+i/ffrDAMo/b2z3TORV3p3m1fPx6CnqBZMvxrHl2CCbij+6O1qmq+8AW8+lQuilq3u6dRBkYpt+mRHWsqvMeNqwKBgQDaair8CIEcoCtxlw8lDRJNn7bC9DRiaJLxPYuOHop7lolUy1amd2srREgoEB7FRwC5bki+BsSUffFyix2kUsf4I2dLHYmbf4Aci2GpqdRW4AnO2tWnvHGsAnkmsRQ2ZuoF7+8Phd1pnXY9DHImAxmpUgqhKDqbP4Hi1W2w5s0Z3QKBgQCTlUxYTq+0AFioGNgrlExSBivWBXTUaVxBghzFGNK2Lkx1d/SgNw/A8T7fAIScUHFcnj5q9Q93DKKXVnge9lR1gaJPsODIDRd7QQKtV+jAcT1M6zxx9x/EObiV7pbjjNtd7zy3ZcNGuIwsgA+5m27JcWAT3JlPYuDwUnFK3EYEjQKBgCHCm1ZNsjdMgqqSIOMnPBcHguZrfNVhOKVVUAbtrZYg1KVosMIWX1hWu5iFtVvk97Wx2EiXHzecp/9+hVxq90HhpwuzSxvf/1tqJ/RjrdCn3Jw+sxu0QxXFZBiY8njeO3ojdh4+INU8Y5RYIiTCAetsJPx4DWcFz/vR5ZyccEN5AoGAHgP5ZeUvn/NR5GvX7NIVbYReO6+YeilNE8mGa57Ew4GJotrS5P4nevDyZWZCs63f4ZQ/I/lJnrGRtQDfQC7wUGhMf7VjZfagFHcSO44uCVKsSO7ToTyuObTpdEC9dUeVaJt96ZP5eX4vWZ6MNgYstlmXKVLg9LHsLJlXKNHufg0="""
        )

        val httpSignatureSignerImpl = HttpSignatureSignerImpl()

        val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        format.timeZone = TimeZone.getTimeZone("GMT")

        //language=JSON
        val requestBody = """{
  "hoge": "fuga"
}"""

        val sha256 = MessageDigest.getInstance("SHA-256")

        val encode = Base64Util.encode(sha256.digest(requestBody.toByteArray()))

        val url = "https://example.com/"
        httpSignatureSignerImpl.sign(
            url,
            HttpMethod.Post,
            Headers.build {
                append("Date", "Fri, 13 Oct 2023 07:14:50 GMT")
                append("Host", URL(url).host)
                append("Digest", "SHA-256=$encode")
            },
            requestBody,
            Key("https://example.com", privateKey, publicKey),
            listOf("(request-target)", "date", "host", "digest")
        )
    }

    @Test
    fun `HTTP Signatureの署名が検証に成功する`() = runTest {
        val publicKey = RsaUtil.decodeRsaPublicKey(
            """MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuJVqbb17nCo8aBZYF+vDgnFANaFDNuvHKMT39qQGnetItYZ8DBtRZzvYE6njn1vH7gixPhGnjt6qLWJJzeoSSv1FgQp9yUq719QFC9BQ87RughpkrP1Nq0ZHuTLMH0U13g2oziRp04FZXElq6b3aHLK+Y78mX20l9HCqIh4GdBRjgiAjcZr/XOZl1cKa7ai3z4yO4euOb8LiJavMHz7/ISefUGtikrhnIqNwwQ1prxT1bZduTotjSi8bitdzsvGh5ftTiFxJC+Pe1yJn3ALW/L3SBm72x60S14osQv1gMaDLaA6YNXCYm34xKndF+UxWTUwLUpNM/GRDoNa8Yq7HBwIDAQAB"""
        )
        val privateKey = RsaUtil.decodeRsaPrivateKey(
            """MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC4lWptvXucKjxoFlgX68OCcUA1oUM268coxPf2pAad60i1hnwMG1FnO9gTqeOfW8fuCLE+EaeO3qotYknN6hJK/UWBCn3JSrvX1AUL0FDztG6CGmSs/U2rRke5MswfRTXeDajOJGnTgVlcSWrpvdocsr5jvyZfbSX0cKoiHgZ0FGOCICNxmv9c5mXVwprtqLfPjI7h645vwuIlq8wfPv8hJ59Qa2KSuGcio3DBDWmvFPVtl25Oi2NKLxuK13Oy8aHl+1OIXEkL497XImfcAtb8vdIGbvbHrRLXiixC/WAxoMtoDpg1cJibfjEqd0X5TFZNTAtSk0z8ZEOg1rxirscHAgMBAAECggEAU5VRQs09Rpt3jBimHnrjptM6pK5X/ewpXKRItpZS6rqqy4xQ6riKFYmrUEgrazOH5ploDTe4XMEmZXOvAP/f9bYXfZXvHLHrOpHnERDtP1XyfpaOBSmUvJyQCORgOz6/ZERiLqqdgyl8+gXC1IJkXH9yKD/cE/UcbUKBP/7BpFj7lPMyNCApiS1Z2RinvOSsx2TCBfVLpEE1dTLdHg3g3vfkmnn+KQ/SU4z3ksXJa0ODZY9lsUGWUrGmnhd/tviSuNUJG3wx7h1er4LBjuA4OZD8qJA+sXcEY2Kn7XQHAOBWUfAOR7nzAl3mPYycIZs4sDrq2awwX12ML9qR/40swQKBgQDtBhIML+Xt32fLw4/wtSDmDJo4szyu0c3Gangl4eMjOc1WEXl/bL8uryNS9b+1he8b+VgEBFH2nhl3u1eman0/xpk9hqj9hd/IDazMqUr7mKq+b9WXWd24LFZNew+35RUELW01FdEDSr+KZsCIjFilAeWfpJORoj3oZFU5C/5mQQKBgQDHXI7NqHy2ATqDiQI3aG72B8n3TbR9B8G01Anfn3ZKcXIFWnDHoB9y/ITYzGrjrbbEOD2BsAacOy7bOWHlX1RIcD10ZWJIBdjqc+zfpahb36mXbcEQkb7col5s992KGVZHu8OBwfGJMVHYprIxOmygj1CAF9pEZyMy3alHChOrRwKBgQCYeyxHHNVNh0huBLxn/Q5SEM9yJJSoXp6Dw+DRdhU6hyf687j26c3ASblu2Fvhem1N0MX3p5PXFPSLW0FS9PTof2n789JpbqN9Ppbo/wwW+ar2YlnFSXHi1tsac020XzJ7AoJcAVH6TS8V6W55KdipJqRDZIvux7IN++X7kiSyQQKBgQCweIIAEhCym0vMe0729P6j0ik5PBN0SZVyF+/VfzYal2kyy+fhDSBJjLWbovdLKs4Jyy7GyaZQTSMg8x5xB3130cLUcZoZ3vMwNgWLwvvQt59LZ9/qZtjoPOIQ2yfDwsHZJZ/eEGtZ4cptWMGLSgg16CZ9/J88xX8m24eoVocqqQKBgCEj/FK26bBLnPtRlQ+5mTQ/CjcjD5/KoaHLawULvXq03qIiZfDZg+sm7JUmlaC48sERGLJnjNYk/1pjw5N8txyAk2UHxqi+dayRkTCRSfBm0PUWyVWiperHNEuByHnyh+qX00sE3SCz2qDSDLb1x7kV+2BhEL+XfgD7evqrvrNq"""
        )

        val httpSignatureSignerImpl = HttpSignatureSignerImpl()

        val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        format.timeZone = TimeZone.getTimeZone("GMT")

        //language=JSON
        val requestBody = """{
  "hoge": "fuga"
}"""

        val sha256 = MessageDigest.getInstance("SHA-256")

        val encode = Base64Util.encode(sha256.digest(requestBody.toByteArray()))

        val url = "https://example.com/"
        val headers = Headers.build {
            append("Date", "Fri, 13 Oct 2023 07:14:50 GMT")
            append("Host", URL(url).host)
            append("Digest", "SHA-256=$encode")
        }
        val sign = httpSignatureSignerImpl.sign(
            url,
            HttpMethod.Post,
            headers,
            requestBody,
            Key("https://example.com", privateKey, publicKey),
            listOf("(request-target)", "date", "host", "digest")
        )

        val keyMap = object : KeyMap {
            override fun getPublicKey(keyId: String?): PublicKey {
                return publicKey
            }

            override fun getPrivateKey(keyId: String?): PrivateKey {
                return privateKey
            }

            override fun getSecretKey(keyId: String?): SecretKey {
                TODO("Not yet implemented")
            }

        }
        val verifier = SignatureHeaderVerifier.builder().keyMap(keyMap).build()

        val headers1 = headers {
            appendAll(headers)
            append("Signature", sign.sign.signatureHeader)
        }

        val httpMessage = object : HttpMessage, HttpRequest {
            override fun headerValues(name: String?): MutableList<String> {
                return name?.let { headers1.getAll(it) }.orEmpty().toMutableList()
            }

            override fun addHeader(name: String?, value: String?) {
                TODO("Not yet implemented")
            }

            override fun method(): String {
                return "POST"
            }

            override fun uri(): URI {
                return URI(url)
            }
        }
        val verify = verifier.verify(httpMessage)
        assertTrue(verify)
    }

    @Test
    fun `HTTP Signatureの署名が検証に成功する2`() = runTest {
        val publicKey = RsaUtil.decodeRsaPublicKeyPem(
            """-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq3YdxpopDvAIp+Ciplvx
SfY8tV3GquYIfxSfTPAqiusgf8zXxYz0ilxY+nHjzIpdOA8rDHcDVhBXI/5lP1Vl
sgeY5cgJRuG9g9ZWaQV/8oKYoillgTkNuyNB0OGa84BAeKo+VMG1NNtlVCn2DrvA
8FLXAc2e4wPcOozKV5JYHZ0RDcSIS1bPb5ArxhhF8zAjn9+s/plsDz+mgHD0Ce5z
UUv1uHQF8nj53WL4cCcrl5TSvqaK6Krcmb7i1YVSlk52p0AYg79pXpPQLhe3TnvJ
Gy+KPvKPq1cho5jM1vJktK6eGlnUPEgD0bCSXl7FrtE7mPMCsaQCRj+up4t+NBWu
gwIDAQAB
-----END PUBLIC KEY-----"""
        )
        val privateKey = RsaUtil.decodeRsaPrivateKeyPem(
            """-----BEGIN PRIVATE KEY-----
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCrdh3GmikO8Ain
4KKmW/FJ9jy1Xcaq5gh/FJ9M8CqK6yB/zNfFjPSKXFj6cePMil04DysMdwNWEFcj
/mU/VWWyB5jlyAlG4b2D1lZpBX/ygpiiKWWBOQ27I0HQ4ZrzgEB4qj5UwbU022VU
KfYOu8DwUtcBzZ7jA9w6jMpXklgdnRENxIhLVs9vkCvGGEXzMCOf36z+mWwPP6aA
cPQJ7nNRS/W4dAXyePndYvhwJyuXlNK+poroqtyZvuLVhVKWTnanQBiDv2lek9Au
F7dOe8kbL4o+8o+rVyGjmMzW8mS0rp4aWdQ8SAPRsJJeXsWu0TuY8wKxpAJGP66n
i340Fa6DAgMBAAECggEAUsE0h9l5/aKumtAZ0K9JmwgErwiuzWcvLJ64cDruXZQ0
YFpuvgNVN75wl5gGeX9ClL8FaQO8EXrbhBzRoyrFZZKzIhxVFef4PzxhAllMMrED
mCjgu+jcjrjqmDV7QxFgjJymbuP7YKKPmnqSLvRBn/xrl4w1pp4DWiL/uhqA+vE8
ZOgfzJ6LzU3CUFjCEi73gfZzTyykzpw+H3Lf8WPYCRQteng7zGxFDpPM3uDt0AKV
nTReopN6HKVOqobBuJLbD2kORfFzfzfLKrkAELivO/yOdosbG5GIf8nxZ0h86QIo
knav6boRgF9LqZTzC+QWBjGXEng58gEYEuAaovup8QKBgQDeR9onVIj67FZ/J1k4
VBTfxRZ4r2oFHyhh3O2Y1xmVM0ejlvtnQL989d6HCieT6wd9CcfTOnTidgXCW+1a
wW3Q6eqtaPanRsU8aCcG2Pa19hbEkdsAvu/8eS8SWegnyqk0lKZjRP6KXDto99dd
CWs8KMcTXTqpFfNr83AeuR1ViwKBgQDFeLms7hvnLVF0oS6LIh73WVd1YfhcCsxo
MfjLmsivCfvyo/RAWmWjHTvh9ofYm3a/1gU4ACm33tI++uWz1juHxJFy+ryjjz7z
MHimmohaWkeax9wyUn66hG52JYUHQFoi85cL/YLMMX3WZXa5LQyyXPgirF4L9+c9
MTZNrKDZ6QKBgEhDX77NksLQtsYbyruvSiH9dvLBRFxp5rz6EBxSQbTpuO6MFSta
N2auoCuSt481J3gVB+u542oEKJcpP57zp3n1sh+yMg3ryg97ZMSrIHnDiV9ac7Jo
YKjZ1N3IcNsO3beEZBt9wKrGlWHowRE0ELK8Jww6kOmLg1mjCN5UHB9FAoGAVewl
vl0MvxY07y6C9f8uwimZqHWsf0AjmOLFgrIiyCbr/bPhP28V8ldyCuweR929WdNi
Ce/oNx05FjZNZGa/GGAreYAoPHLDzUU1+igbVFUb+vkjkrHaeoXNGpNQwsr5bWPY
QVtZYkfWnUcg1YoIkENrpIqjkUmY0ENtgXavtqECgYEA2F+FJPPpm39gD2mnbnAH
goM9c+h9hh/o3kW3CUNgPKeYT4ptd3AG0k9C9De+eWb3GGqH1/KUGvUbyXm7f1Wi
y+SBT1Uk6/85ZZ3nCz2Yj8eGokhcfKhXd8K3HV2wgoUWMJT1Qvedrqc2R5S9wdY8
wADggCG8df/amNR+dyQOOuQ=
-----END PRIVATE KEY-----"""
        )

        val httpSignatureSignerImpl = HttpSignatureSignerImpl()

        val format = DateTimeFormatter.RFC_1123_DATE_TIME

        //language=JSON
        val requestBody = """{
  "hoge": "fuga"
}"""

        val sha256 = MessageDigest.getInstance("SHA-256")

        val encode = Base64Util.encode(sha256.digest(requestBody.toByteArray()))

        val url = "https://test-hideout.usbharu.dev/users/97ws8y3rj6/inbox"
        val headers = Headers.build {
            append("Date", format.format(ZonedDateTime.now(ZoneId.of("GMT"))))
            append("Host", URL(url).host)
            append("Digest", "sha-256=$encode")
        }
        val sign = httpSignatureSignerImpl.sign(
            url,
            HttpMethod.Post,
            headers,
            requestBody,
            Key("https://test-hideout.usbharu.dev/users/c#pubkey", privateKey, publicKey),
            listOf("(request-target)", "date", "host", "digest")
        )

        val keyMap = object : KeyMap {
            override fun getPublicKey(keyId: String?): PublicKey {
                return publicKey
            }

            override fun getPrivateKey(keyId: String?): PrivateKey {
                return privateKey
            }

            override fun getSecretKey(keyId: String?): SecretKey {
                TODO("Not yet implemented")
            }

        }
        val verifier = SignatureHeaderVerifier.builder().keyMap(keyMap).build()

        val headers1 = headers {
            appendAll(headers)
            append("Signature", sign.sign.signatureHeader)
        }

        val httpMessage = object : HttpMessage, HttpRequest {
            override fun headerValues(name: String?): MutableList<String> {
                return name?.let { headers1.getAll(it) }.orEmpty().toMutableList()
            }

            override fun addHeader(name: String?, value: String?) {
                TODO("Not yet implemented")
            }

            override fun method(): String {
                return "POST"
            }

            override fun uri(): URI {
                return URI(url)
            }
        }
        val verify = verifier.verify(httpMessage)
        assertTrue(verify)
    }

    @Test
    fun `HTTP Signatureで署名した後、改ざんされた場合検証に失敗する`() = runTest {
        val publicKey = RsaUtil.decodeRsaPublicKey(
            """MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuJVqbb17nCo8aBZYF+vDgnFANaFDNuvHKMT39qQGnetItYZ8DBtRZzvYE6njn1vH7gixPhGnjt6qLWJJzeoSSv1FgQp9yUq719QFC9BQ87RughpkrP1Nq0ZHuTLMH0U13g2oziRp04FZXElq6b3aHLK+Y78mX20l9HCqIh4GdBRjgiAjcZr/XOZl1cKa7ai3z4yO4euOb8LiJavMHz7/ISefUGtikrhnIqNwwQ1prxT1bZduTotjSi8bitdzsvGh5ftTiFxJC+Pe1yJn3ALW/L3SBm72x60S14osQv1gMaDLaA6YNXCYm34xKndF+UxWTUwLUpNM/GRDoNa8Yq7HBwIDAQAB"""
        )
        val privateKey = RsaUtil.decodeRsaPrivateKey(
            """MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC4lWptvXucKjxoFlgX68OCcUA1oUM268coxPf2pAad60i1hnwMG1FnO9gTqeOfW8fuCLE+EaeO3qotYknN6hJK/UWBCn3JSrvX1AUL0FDztG6CGmSs/U2rRke5MswfRTXeDajOJGnTgVlcSWrpvdocsr5jvyZfbSX0cKoiHgZ0FGOCICNxmv9c5mXVwprtqLfPjI7h645vwuIlq8wfPv8hJ59Qa2KSuGcio3DBDWmvFPVtl25Oi2NKLxuK13Oy8aHl+1OIXEkL497XImfcAtb8vdIGbvbHrRLXiixC/WAxoMtoDpg1cJibfjEqd0X5TFZNTAtSk0z8ZEOg1rxirscHAgMBAAECggEAU5VRQs09Rpt3jBimHnrjptM6pK5X/ewpXKRItpZS6rqqy4xQ6riKFYmrUEgrazOH5ploDTe4XMEmZXOvAP/f9bYXfZXvHLHrOpHnERDtP1XyfpaOBSmUvJyQCORgOz6/ZERiLqqdgyl8+gXC1IJkXH9yKD/cE/UcbUKBP/7BpFj7lPMyNCApiS1Z2RinvOSsx2TCBfVLpEE1dTLdHg3g3vfkmnn+KQ/SU4z3ksXJa0ODZY9lsUGWUrGmnhd/tviSuNUJG3wx7h1er4LBjuA4OZD8qJA+sXcEY2Kn7XQHAOBWUfAOR7nzAl3mPYycIZs4sDrq2awwX12ML9qR/40swQKBgQDtBhIML+Xt32fLw4/wtSDmDJo4szyu0c3Gangl4eMjOc1WEXl/bL8uryNS9b+1he8b+VgEBFH2nhl3u1eman0/xpk9hqj9hd/IDazMqUr7mKq+b9WXWd24LFZNew+35RUELW01FdEDSr+KZsCIjFilAeWfpJORoj3oZFU5C/5mQQKBgQDHXI7NqHy2ATqDiQI3aG72B8n3TbR9B8G01Anfn3ZKcXIFWnDHoB9y/ITYzGrjrbbEOD2BsAacOy7bOWHlX1RIcD10ZWJIBdjqc+zfpahb36mXbcEQkb7col5s992KGVZHu8OBwfGJMVHYprIxOmygj1CAF9pEZyMy3alHChOrRwKBgQCYeyxHHNVNh0huBLxn/Q5SEM9yJJSoXp6Dw+DRdhU6hyf687j26c3ASblu2Fvhem1N0MX3p5PXFPSLW0FS9PTof2n789JpbqN9Ppbo/wwW+ar2YlnFSXHi1tsac020XzJ7AoJcAVH6TS8V6W55KdipJqRDZIvux7IN++X7kiSyQQKBgQCweIIAEhCym0vMe0729P6j0ik5PBN0SZVyF+/VfzYal2kyy+fhDSBJjLWbovdLKs4Jyy7GyaZQTSMg8x5xB3130cLUcZoZ3vMwNgWLwvvQt59LZ9/qZtjoPOIQ2yfDwsHZJZ/eEGtZ4cptWMGLSgg16CZ9/J88xX8m24eoVocqqQKBgCEj/FK26bBLnPtRlQ+5mTQ/CjcjD5/KoaHLawULvXq03qIiZfDZg+sm7JUmlaC48sERGLJnjNYk/1pjw5N8txyAk2UHxqi+dayRkTCRSfBm0PUWyVWiperHNEuByHnyh+qX00sE3SCz2qDSDLb1x7kV+2BhEL+XfgD7evqrvrNq"""
        )

        val httpSignatureSignerImpl = HttpSignatureSignerImpl()

        val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        format.timeZone = TimeZone.getTimeZone("GMT")

        //language=JSON
        val requestBody = """{
  "hoge": "fuga"
}"""

        val sha256 = MessageDigest.getInstance("SHA-256")

        val encode = Base64Util.encode(sha256.digest(requestBody.toByteArray()))

        val url = "https://example.com/"
        val headers = Headers.build {
            append("Date", "Fri, 13 Oct 2023 07:14:50 GMT")
            append("Host", URL(url).host)
            append("Digest", "SHA-256=$encode")
        }
        val sign = httpSignatureSignerImpl.sign(
            url,
            HttpMethod.Post,
            headers,
            requestBody,
            Key("https://example.com", privateKey, publicKey),
            listOf("(request-target)", "date", "host", "digest")
        )

        val keyMap = object : KeyMap {
            override fun getPublicKey(keyId: String?): PublicKey {
                return publicKey
            }

            override fun getPrivateKey(keyId: String?): PrivateKey {
                return privateKey
            }

            override fun getSecretKey(keyId: String?): SecretKey {
                TODO("Not yet implemented")
            }

        }
        val verifier = SignatureHeaderVerifier.builder().keyMap(keyMap).build()

        val headers1 = headers {
            appendAll(headers)
            append("Signature", sign.sign.signatureHeader)
            set("Digest", "aaaaaaaaaaaaaaaaafsadasfgafaaaaaaaaaaa")
        }

        val httpMessage = object : HttpMessage, HttpRequest {
            override fun headerValues(name: String?): MutableList<String> {
                return name?.let { headers1.getAll(it) }.orEmpty().toMutableList()
            }

            override fun addHeader(name: String?, value: String?) {
                TODO("Not yet implemented")
            }

            override fun method(): String {
                return "POST"
            }

            override fun uri(): URI {
                return URI(url)
            }
        }
        val verify = verifier.verify(httpMessage)
        assertFalse(verify)
    }
}
