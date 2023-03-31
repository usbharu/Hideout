package dev.usbharu.hideout.plugins

import dev.usbharu.hideout.ap.JsonLd
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.repository.IUserAuthRepository
import dev.usbharu.hideout.service.IUserAuthService
import dev.usbharu.hideout.service.UserAuthService
import dev.usbharu.hideout.util.HttpUtil.Activity
import io.ktor.client.*
import io.ktor.client.plugins.api.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.*
import io.netty.handler.codec.base64.Base64
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.inject

import org.koin.ktor.ext.inject
import tech.barbero.http.message.signing.HttpMessage
import tech.barbero.http.message.signing.HttpMessageSigner
import tech.barbero.http.message.signing.HttpRequest
import tech.barbero.http.message.signing.KeyMap
import java.net.URI
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.SecretKey

suspend fun <T : JsonLd> ApplicationCall.respondAp(message: T, status: HttpStatusCode = HttpStatusCode.OK) {
    message.context += "https://www.w3.org/ns/activitystreams"
    val activityJson = Config.configData.objectMapper.writeValueAsString(message)
    respondText(activityJson, ContentType.Application.Activity, status)
}

suspend fun HttpClient.postAp(urlString: String,username:String,jsonLd: JsonLd): HttpResponse {
    return this.post(urlString){
        header("Accept", ContentType.Application.Activity)
        header("Content-Type", ContentType.Application.Activity)
        header("Signature","keyId=\"$username\",algorithm=\"rsa-sha256\",headers=\"(request-target) digest date\"")
        val text = Config.configData.objectMapper.writeValueAsString(jsonLd)
        setBody(text)
    }
}

class HttpSignaturePluginConfig {
    lateinit var keyMap: KeyMap
}

val httpSignaturePlugin = createClientPlugin("HttpSign",::HttpSignaturePluginConfig) {
    val keyMap = pluginConfig.keyMap
    val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
    onRequest { request, body ->


        request.header("Date", format.format(Date()))
        println(request.bodyType)
        println(request.bodyType?.type)
        if (request.bodyType?.type == String::class) {
            println("Digest !!")
            val digest =
                hex(UserAuthService.sha256.digest((body as String).toByteArray(Charsets.UTF_8)))
            request.headers.append("Digest", digest)
        }

        if (request.headers.contains("Signature")) {
            val all = request.headers.getAll("Signature")!!
            val parameters = mutableListOf<String>()
            for (s in all) {
                s.split(",").forEach { parameters.add(it) }
            }

            val keyId = parameters.find { it.startsWith("keyId") }?.split("=")?.get(1)?.replace("\"", "")
            val algorithm =
                parameters.find { it.startsWith("algorithm") }?.split("=")?.get(1)?.replace("\"", "")
            val headers = parameters.find { it.startsWith("headers") }?.split("=")?.get(1)?.replace("\"", "")
                ?.split(" ")?.toMutableList().orEmpty()

            val algorithmType = when (algorithm) {
                "rsa-sha256" -> {
                    HttpMessageSigner.Algorithm.RSA_SHA256
                }

                else -> {
                    TODO()
                }
            }

            headers.map {
                when (it) {
                    "(request-target)" -> {
                        HttpMessageSigner.REQUEST_TARGET
                    }

                    "digest" -> {
                        "Digest"
                    }

                    "date" -> {
                        "Date"
                    }

                    else -> {
                        it
                    }
                }
            }

            val builder = HttpMessageSigner.builder().algorithm(algorithmType).keyId("${Config.configData.url}/users/$keyId/pubkey").keyMap(keyMap)
            var tmp = builder
            headers.forEach {
                tmp = tmp.addHeaderToSign(it)
            }
            val signer = tmp.build()

            request.headers.remove("Signature")

            signer!!.sign(object : HttpMessage,HttpRequest {
                override fun headerValues(name: String?): MutableList<String> {
                    return name?.let { request.headers.getAll(it) }?.toMutableList() ?: mutableListOf()
                }

                override fun addHeader(name: String?, value: String?) {
                    name?.let { request.header(it,value) }
                }

                override fun method(): String {
                    return request.method.value
                }

                override fun uri(): URI {
                    return request.url.build().toURI()
                }


            })

        }

    }
}

class KtorKeyMap(private val userAuthRepository: IUserAuthService) : KeyMap {
    override fun getPublicKey(keyId: String?): PublicKey = runBlocking {
        val publicBytes = java.util.Base64.getDecoder().decode(
            userAuthRepository.findByUsername(
                (keyId ?: throw IllegalArgumentException("keyId is null"))
            ).publicKey
        )
        val x509EncodedKeySpec = X509EncodedKeySpec(publicBytes)
        return@runBlocking KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec)
    }

    override fun getPrivateKey(keyId: String?): PrivateKey = runBlocking {
        val publicBytes = java.util.Base64.getDecoder().decode(
            userAuthRepository.findByUsername(
                (keyId ?: throw IllegalArgumentException("keyId is null"))
            ).privateKey
        )
        val x509EncodedKeySpec = X509EncodedKeySpec(publicBytes)
        return@runBlocking KeyFactory.getInstance("RSA").generatePrivate(x509EncodedKeySpec)
    }

    override fun getSecretKey(keyId: String?): SecretKey {
        TODO("Not yet implemented")
    }
}
