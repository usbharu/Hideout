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

package dev.usbharu.hideout.core.domain.model.actor

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.config.CharacterLimit
import jakarta.validation.Validator
import jakarta.validation.constraints.*
import org.hibernate.validator.constraints.URL
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import kotlin.math.max

data class Actor private constructor(
    @get:NotNull
    @get:Positive
    val id: Long,
    @get:Pattern(regexp = "^[a-zA-Z0-9_-]{1,300}\$")
    @get:Size(min = 1)
    val name: String,
    @get:Pattern(regexp = "^([a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]*\\.){1,255}[a-zA-Z]{2,}\$")
    val domain: String,
    val screenName: String,
    val description: String,
    @get:URL
    val inbox: String,
    @get:URL
    val outbox: String,
    @get:URL
    val url: String,
    @get:NotBlank
    val publicKey: String,
    val privateKey: String? = null,
    @get:PastOrPresent
    val createdAt: Instant,
    @get:NotBlank
    val keyId: String,
    val followers: String? = null,
    val following: String? = null,
    @get:Positive
    val instance: Long? = null,
    val locked: Boolean,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val lastPostDate: Instant? = null,
    val emojis: List<Long> = emptyList(),
) {

    @Component
    class UserBuilder(
        private val characterLimit: CharacterLimit,
        private val applicationConfig: ApplicationConfig,
        private val validator: Validator,
    ) {

        private val logger = LoggerFactory.getLogger(UserBuilder::class.java)

        @Suppress("LongParameterList", "FunctionMinLength", "LongMethod")
        fun of(
            id: Long,
            name: String,
            domain: String,
            screenName: String,
            description: String,
            inbox: String,
            outbox: String,
            url: String,
            publicKey: String,
            privateKey: String? = null,
            createdAt: Instant,
            keyId: String,
            following: String? = null,
            followers: String? = null,
            instance: Long? = null,
            locked: Boolean,
            followersCount: Int = 0,
            followingCount: Int = 0,
            postsCount: Int = 0,
            lastPostDate: Instant? = null,
            emojis: List<Long> = emptyList(),
        ): Actor {
            if (id == 0L) {
                return Actor(
                    id = id,
                    name = name,
                    domain = domain,
                    screenName = screenName,
                    description = description,
                    inbox = inbox,
                    outbox = outbox,
                    url = url,
                    publicKey = publicKey,
                    privateKey = privateKey,
                    createdAt = createdAt,
                    keyId = keyId,
                    followers = followers,
                    following = following,
                    instance = instance,
                    locked = locked,
                    followersCount = followersCount,
                    followingCount = followingCount,
                    postsCount = postsCount,
                    lastPostDate = lastPostDate,
                    emojis = emojis
                )
            }

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

            val actor = Actor(
                id = id,
                name = limitedName,
                domain = domain,
                screenName = limitedScreenName,
                description = limitedDescription,
                inbox = inbox,
                outbox = outbox,
                url = url,
                publicKey = publicKey,
                privateKey = privateKey,
                createdAt = createdAt,
                keyId = keyId,
                followers = followers,
                following = following,
                instance = instance,
                locked = locked,
                followersCount = max(0, followersCount),
                followingCount = max(0, followingCount),
                postsCount = max(0, postsCount),
                lastPostDate = lastPostDate,
                emojis = emojis
            )

            val validate = validator.validate(actor)

            for (constraintViolation in validate) {
                throw IllegalArgumentException("${constraintViolation.propertyPath} : ${constraintViolation.message}")
            }
            return actor
        }
    }

    fun incrementFollowing(): Actor = this.copy(followingCount = this.followingCount + 1)

    fun decrementFollowing(): Actor = this.copy(followingCount = this.followingCount - 1)

    fun incrementFollowers(): Actor = this.copy(followersCount = this.followersCount + 1)

    fun decrementFollowers(): Actor = this.copy(followersCount = this.followersCount - 1)

    fun incrementPostsCount(): Actor = this.copy(postsCount = this.postsCount + 1)

    fun decrementPostsCount(): Actor = this.copy(postsCount = this.postsCount - 1)

    fun withLastPostAt(lastPostDate: Instant): Actor = this.copy(lastPostDate = lastPostDate)
    override fun toString(): String {
        return "Actor(" +
                "id=$id, " +
                "name='$name', " +
                "domain='$domain', " +
                "screenName='$screenName', " +
                "description='$description', " +
                "inbox='$inbox', " +
                "outbox='$outbox', " +
                "url='$url', " +
                "publicKey='$publicKey', " +
                "privateKey=$privateKey, " +
                "createdAt=$createdAt, " +
                "keyId='$keyId', " +
                "followers=$followers, " +
                "following=$following, " +
                "instance=$instance, " +
                "locked=$locked, " +
                "followersCount=$followersCount, " +
                "followingCount=$followingCount, " +
                "postsCount=$postsCount, " +
                "lastPostDate=$lastPostDate, " +
                "emojis=$emojis" +
                ")"
    }
}
