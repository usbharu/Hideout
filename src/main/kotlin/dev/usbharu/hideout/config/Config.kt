package dev.usbharu.hideout.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object Config {
    var configData: ConfigData = ConfigData()
}

data class ConfigData(
    val url: String = "",
    val domain: String = url.substringAfter("://").substringBeforeLast(":"),
    val objectMapper: ObjectMapper = jacksonObjectMapper(),
    val characterLimit: CharacterLimit = CharacterLimit()
)

data class CharacterLimit(
    val general: General = General.of(),
    val post: Post = Post(),
    val account: Account = Account(),
    val instance: Instance = Instance()
) {
    data class General private constructor(
        val url: Int,
        val domain: Int,
        val publicKey: Int,
        val privateKey: Int
    ) {
        companion object {
            @Suppress("FunctionMinLength")
            fun of(url: Int? = null, domain: Int? = null, publicKey: Int? = null, privateKey: Int? = null): General {
                return General(
                    url ?: 1000,
                    domain ?: 1000,
                    publicKey ?: 10000,
                    privateKey ?: 10000
                )
            }
        }
    }

    data class Post(
        val text: Int = 3000,
        val overview: Int = 3000
    )

    data class Account(
        val id: Int = 300,
        val name: Int = 300,
        val description: Int = 10000
    )

    data class Instance(
        val name: Int = 600,
        val description: Int = 10000
    )
}
