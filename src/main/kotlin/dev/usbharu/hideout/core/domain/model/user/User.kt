package dev.usbharu.hideout.core.domain.model.user

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.config.CharacterLimit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant

data class User private constructor(
    val id: Long,
    val name: String,
    val domain: String,
    val screenName: String,
    val description: String,
    val password: String? = null,
    val inbox: String,
    val outbox: String,
    val url: String,
    val publicKey: String,
    val privateKey: String? = null,
    val createdAt: Instant,
    val keyId: String,
    val followers: String? = null,
    val following: String? = null,
    val instance: Long? = null
) {
    override fun toString(): String =
        "User(id=$id, name='$name', domain='$domain', screenName='$screenName', description='$description', password=$password, inbox='$inbox', outbox='$outbox', url='$url', publicKey='$publicKey', privateKey=$privateKey, createdAt=$createdAt, keyId='$keyId', followers=$followers, following=$following, instance=$instance)"

    @Component
    class UserBuilder(private val characterLimit: CharacterLimit, private val applicationConfig: ApplicationConfig) {

        private val logger = LoggerFactory.getLogger(UserBuilder::class.java)

        @Suppress("LongParameterList", "FunctionMinLength", "LongMethod")
        fun of(
            id: Long,
            name: String,
            domain: String,
            screenName: String,
            description: String,
            password: String? = null,
            inbox: String,
            outbox: String,
            url: String,
            publicKey: String,
            privateKey: String? = null,
            createdAt: Instant,
            keyId: String,
            following: String? = null,
            followers: String? = null,
            instance: Long? = null
        ): User {
            // idは0未満ではいけない
            require(id >= 0) { "id must be greater than or equal to 0." }

            // nameは空文字以外を含める必要がある
            require(name.isNotBlank()) { "name must contain non-blank characters." }

            // nameは指定された長さ以下である必要がある
            val limitedName = if (name.length >= characterLimit.account.id) {
                logger.warn("name must not exceed ${characterLimit.account.id} characters.")
                name.substring(0, characterLimit.account.id)
            } else {
                name
            }

            // domainは空文字以外を含める必要がある
            require(domain.isNotBlank()) { "domain must contain non-blank characters." }

            // domainは指定された長さ以下である必要がある
            require(domain.length <= characterLimit.general.domain) {
                "domain must not exceed ${characterLimit.general.domain} characters."
            }

            // screenNameは空文字以外を含める必要がある
            require(screenName.isNotBlank()) { "screenName must contain non-blank characters." }

            // screenNameは指定された長さ以下である必要がある
            val limitedScreenName = if (screenName.length >= characterLimit.account.name) {
                logger.warn("screenName must not exceed ${characterLimit.account.name} characters.")
                screenName.substring(0, characterLimit.account.name)
            } else {
                screenName
            }

            // descriptionは指定された長さ以下である必要がある
            val limitedDescription = if (description.length >= characterLimit.account.description) {
                logger.warn("description must not exceed ${characterLimit.account.description} characters.")
                description.substring(0, characterLimit.account.description)
            } else {
                description
            }

            // ローカルユーザーはpasswordとprivateKeyをnullにしてはいけない
            if (domain == applicationConfig.url.host) {
                requireNotNull(password) { "password and privateKey must not be null for local users." }
                requireNotNull(privateKey) { "password and privateKey must not be null for local users." }
            }

            // urlは空文字以外を含める必要がある
            require(url.isNotBlank()) { "url must contain non-blank characters." }

            // urlは指定された長さ以下である必要がある
            require(url.length <= characterLimit.general.url) {
                "url must not exceed ${characterLimit.general.url} characters."
            }

            // inboxは空文字以外を含める必要がある
            require(inbox.isNotBlank()) { "inbox must contain non-blank characters." }

            // inboxは指定された長さ以下である必要がある
            require(inbox.length <= characterLimit.general.url) {
                "inbox must not exceed ${characterLimit.general.url} characters."
            }

            // outboxは空文字以外を含める必要がある
            require(outbox.isNotBlank()) { "outbox must contain non-blank characters." }

            // outboxは指定された長さ以下である必要がある
            require(outbox.length <= characterLimit.general.url) {
                "outbox must not exceed ${characterLimit.general.url} characters."
            }

            require(keyId.isNotBlank()) {
                "keyId must contain non-blank characters."
            }

            return User(
                id = id,
                name = limitedName,
                domain = domain,
                screenName = limitedScreenName,
                description = limitedDescription,
                password = password,
                inbox = inbox,
                outbox = outbox,
                url = url,
                publicKey = publicKey,
                privateKey = privateKey,
                createdAt = createdAt,
                keyId = keyId,
                followers = followers,
                following = following,
                instance = instance
            )
        }
    }
}
