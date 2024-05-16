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

package dev.usbharu.hideout.mastodon.config

import dev.usbharu.hideout.application.infrastructure.springframework.RoleHierarchyAuthorizationManagerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod.*
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain

@Configuration
class MastodonApiSecurityConfig {
    @Bean
    @Order(4)
    @Suppress("LongMethod")
    fun mastodonApiSecurityFilterChain(
        http: HttpSecurity,
        rf: RoleHierarchyAuthorizationManagerFactory,
    ): SecurityFilterChain {
        http {
            securityMatcher("/api/v1/**", "/api/v2/**")
            authorizeHttpRequests {
                authorize(POST, "/api/v1/apps", permitAll)
                authorize(GET, "/api/v1/instance/**", permitAll)
                authorize(POST, "/api/v1/accounts", authenticated)

                authorize(GET, "/api/v1/accounts/verify_credentials", rf.hasScope("read:accounts"))
                authorize(GET, "/api/v1/accounts/relationships", rf.hasScope("read:follows"))
                authorize(GET, "/api/v1/accounts/*", permitAll)
                authorize(GET, "/api/v1/accounts/*/statuses", permitAll)
                authorize(POST, "/api/v1/accounts/*/follow", rf.hasScope("write:follows"))
                authorize(POST, "/api/v1/accounts/*/unfollow", rf.hasScope("write:follows"))
                authorize(POST, "/api/v1/accounts/*/block", rf.hasScope("write:blocks"))
                authorize(POST, "/api/v1/accounts/*/unblock", rf.hasScope("write:blocks"))
                authorize(POST, "/api/v1/accounts/*/mute", rf.hasScope("write:mutes"))
                authorize(POST, "/api/v1/accounts/*/unmute", rf.hasScope("write:mutes"))
                authorize(GET, "/api/v1/mutes", rf.hasScope("read:mutes"))

                authorize(POST, "/api/v1/media", rf.hasScope("write:media"))
                authorize(POST, "/api/v1/statuses", rf.hasScope("write:statuses"))
                authorize(GET, "/api/v1/statuses/*", permitAll)
                authorize(POST, "/api/v1/statuses/*/favourite", rf.hasScope("write:favourites"))
                authorize(POST, "/api/v1/statuses/*/unfavourite", rf.hasScope("write:favourites"))
                authorize(PUT, "/api/v1/statuses/*/emoji_reactions/*", rf.hasScope("write:favourites"))

                authorize(GET, "/api/v1/timelines/public", permitAll)
                authorize(GET, "/api/v1/timelines/home", rf.hasScope("read:statuses"))

                authorize(GET, "/api/v2/filters", rf.hasScope("read:filters"))
                authorize(POST, "/api/v2/filters", rf.hasScope("write:filters"))

                authorize(GET, "/api/v2/filters/*", rf.hasScope("read:filters"))
                authorize(PUT, "/api/v2/filters/*", rf.hasScope("write:filters"))
                authorize(DELETE, "/api/v2/filters/*", rf.hasScope("write:filters"))

                authorize(GET, "/api/v2/filters/*/keywords", rf.hasScope("read:filters"))
                authorize(POST, "/api/v2/filters/*/keywords", rf.hasScope("write:filters"))

                authorize(GET, "/api/v2/filters/keywords/*", rf.hasScope("read:filters"))
                authorize(PUT, "/api/v2/filters/keywords/*", rf.hasScope("write:filters"))
                authorize(DELETE, "/api/v2/filters/keywords/*", rf.hasScope("write:filters"))

                authorize(GET, "/api/v2/filters/*/statuses", rf.hasScope("read:filters"))
                authorize(POST, "/api/v2/filters/*/statuses", rf.hasScope("write:filters"))

                authorize(GET, "/api/v2/filters/statuses/*", rf.hasScope("read:filters"))
                authorize(DELETE, "/api/v2/filters/statuses/*", rf.hasScope("write:filters"))

                authorize(GET, "/api/v1/filters", rf.hasScope("read:filters"))
                authorize(POST, "/api/v1/filters", rf.hasScope("write:filters"))

                authorize(GET, "/api/v1/filters/*", rf.hasScope("read:filters"))
                authorize(POST, "/api/v1/filters/*", rf.hasScope("write:filters"))
                authorize(DELETE, "/api/v1/filters/*", rf.hasScope("write:filters"))

                authorize(GET, "/api/v1/notifications", rf.hasScope("read:notifications"))
                authorize(GET, "/api/v1/notifications/*", rf.hasScope("read:notifications"))
                authorize(POST, "/api/v1/notifications/clear", rf.hasScope("write:notifications"))
                authorize(POST, "/api/v1/notifications/*/dismiss", rf.hasScope("write:notifications"))

                authorize(anyRequest, authenticated)
            }

            oauth2ResourceServer {
                jwt { }
            }

            csrf {
                ignoringRequestMatchers("/api/v1/apps")
            }
        }

        return http.build()
    }

    @Bean
    fun roleHierarchy(): RoleHierarchy {
        val roleHierarchyImpl = RoleHierarchyImpl()

        roleHierarchyImpl.setHierarchy(
            """
            SCOPE_read > SCOPE_read:accounts
            SCOPE_read > SCOPE_read:accounts
            SCOPE_read > SCOPE_read:blocks
            SCOPE_read > SCOPE_read:bookmarks
            SCOPE_read > SCOPE_read:favourites
            SCOPE_read > SCOPE_read:filters
            SCOPE_read > SCOPE_read:follows
            SCOPE_read > SCOPE_read:lists
            SCOPE_read > SCOPE_read:mutes
            SCOPE_read > SCOPE_read:notifications
            SCOPE_read > SCOPE_read:search
            SCOPE_read > SCOPE_read:statuses
            SCOPE_write > SCOPE_write:accounts
            SCOPE_write > SCOPE_write:blocks
            SCOPE_write > SCOPE_write:bookmarks
            SCOPE_write > SCOPE_write:conversations
            SCOPE_write > SCOPE_write:favourites
            SCOPE_write > SCOPE_write:filters
            SCOPE_write > SCOPE_write:follows
            SCOPE_write > SCOPE_write:lists
            SCOPE_write > SCOPE_write:media
            SCOPE_write > SCOPE_write:mutes
            SCOPE_write > SCOPE_write:notifications
            SCOPE_write > SCOPE_write:reports
            SCOPE_write > SCOPE_write:statuses
            SCOPE_follow > SCOPE_write:blocks
            SCOPE_follow > SCOPE_write:follows
            SCOPE_follow > SCOPE_write:mutes
            SCOPE_follow > SCOPE_read:blocks
            SCOPE_follow > SCOPE_read:follows
            SCOPE_follow > SCOPE_read:mutes
            SCOPE_admin > SCOPE_admin:read
            SCOPE_admin > SCOPE_admin:write
            SCOPE_admin:read > SCOPE_admin:read:accounts
            SCOPE_admin:read > SCOPE_admin:read:reports
            SCOPE_admin:read > SCOPE_admin:read:domain_allows
            SCOPE_admin:read > SCOPE_admin:read:domain_blocks
            SCOPE_admin:read > SCOPE_admin:read:ip_blocks
            SCOPE_admin:read > SCOPE_admin:read:email_domain_blocks
            SCOPE_admin:read > SCOPE_admin:read:canonical_email_blocks
            SCOPE_admin:write > SCOPE_admin:write:accounts
            SCOPE_admin:write > SCOPE_admin:write:reports
            SCOPE_admin:write > SCOPE_admin:write:domain_allows
            SCOPE_admin:write > SCOPE_admin:write:domain_blocks
            SCOPE_admin:write > SCOPE_admin:write:ip_blocks
            SCOPE_admin:write > SCOPE_admin:write:email_domain_blocks
            SCOPE_admin:write > SCOPE_admin:write:canonical_email_blocks
            """.trimIndent()
        )

        return roleHierarchyImpl
    }
}
