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

import com.fasterxml.jackson.annotation.JsonInclude
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.application.infrastructure.springframework.RoleHierarchyAuthorizationManagerFactory
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.infrastructure.springframework.httpsignature.HttpSignatureFilter
import dev.usbharu.hideout.core.infrastructure.springframework.httpsignature.HttpSignatureHeaderChecker
import dev.usbharu.hideout.core.infrastructure.springframework.httpsignature.HttpSignatureUserDetailsService
import dev.usbharu.hideout.core.infrastructure.springframework.httpsignature.HttpSignatureVerifierComposite
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.UserDetailsImpl
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.UserDetailsServiceImpl
import dev.usbharu.hideout.util.RsaUtil
import dev.usbharu.httpsignature.sign.RsaSha256HttpSignatureSigner
import dev.usbharu.httpsignature.verify.DefaultSignatureHeaderParser
import dev.usbharu.httpsignature.verify.RsaSha256HttpSignatureVerifier
import jakarta.annotation.PostConstruct
import jakarta.servlet.*
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod.*
import org.springframework.http.HttpStatus
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.authentication.AccountStatusUserDetailsChecker
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolderStrategy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import org.springframework.security.web.FilterChainProxy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.ExceptionTranslationFilter
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer
import org.springframework.security.web.debug.DebugFilter
import org.springframework.security.web.firewall.HttpFirewall
import org.springframework.security.web.firewall.RequestRejectedHandler
import org.springframework.security.web.savedrequest.RequestCacheAwareFilter
import org.springframework.security.web.util.matcher.AnyRequestMatcher
import org.springframework.web.filter.CompositeFilter
import java.io.IOException
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*


@EnableWebSecurity(debug = false)
@Configuration
@Suppress("FunctionMaxLength", "TooManyFunctions", "LongMethod")
class SecurityConfig {

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager? {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    @Order(1)
    fun httpSignatureFilterChain(
        http: HttpSecurity,
        httpSignatureFilter: HttpSignatureFilter,
    ): SecurityFilterChain {
        http {
            securityMatcher("/users/*/posts/*")
            addFilterAt<RequestCacheAwareFilter>(httpSignatureFilter)
            addFilterBefore<HttpSignatureFilter>(
                ExceptionTranslationFilter(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            authorizeHttpRequests {
                authorize(anyRequest, permitAll)
            }
            exceptionHandling {
                authenticationEntryPoint = HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                defaultAuthenticationEntryPointFor(
                    HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    AnyRequestMatcher.INSTANCE
                )
            }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
        }
        return http.build()
    }

    @Bean
    fun getHttpSignatureFilter(
        authenticationManager: AuthenticationManager,
        httpSignatureHeaderChecker: HttpSignatureHeaderChecker,
    ): HttpSignatureFilter {
        val httpSignatureFilter =
            HttpSignatureFilter(DefaultSignatureHeaderParser(), httpSignatureHeaderChecker)
        httpSignatureFilter.setAuthenticationManager(authenticationManager)
        httpSignatureFilter.setContinueFilterChainOnUnsuccessfulAuthentication(false)
        val authenticationEntryPointFailureHandler =
            AuthenticationEntryPointFailureHandler(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        authenticationEntryPointFailureHandler.setRethrowAuthenticationServiceException(false)
        httpSignatureFilter.setAuthenticationFailureHandler(authenticationEntryPointFailureHandler)
        return httpSignatureFilter
    }

    @Bean
    @Order(2)
    fun daoAuthenticationProvider(userDetailsServiceImpl: UserDetailsServiceImpl): DaoAuthenticationProvider {
        val daoAuthenticationProvider = DaoAuthenticationProvider()
        daoAuthenticationProvider.setUserDetailsService(userDetailsServiceImpl)

        return daoAuthenticationProvider
    }

    @Bean
    @Order(1)
    fun httpSignatureAuthenticationProvider(
        transaction: Transaction,
        actorRepository: ActorRepository,
    ): PreAuthenticatedAuthenticationProvider {
        val provider = PreAuthenticatedAuthenticationProvider()
        val signatureHeaderParser = DefaultSignatureHeaderParser()
        provider.setPreAuthenticatedUserDetailsService(
            HttpSignatureUserDetailsService(
                HttpSignatureVerifierComposite(
                    mapOf(
                        "rsa-sha256" to RsaSha256HttpSignatureVerifier(
                            signatureHeaderParser, RsaSha256HttpSignatureSigner()
                        )
                    ),
                    signatureHeaderParser
                ),
                transaction,
                signatureHeaderParser,
                actorRepository
            )
        )
        provider.setUserDetailsChecker(AccountStatusUserDetailsChecker())
        return provider
    }

    @Bean
    @Order(2)
    fun oauth2SecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)
        http {
            exceptionHandling {
                authenticationEntryPoint = LoginUrlAuthenticationEntryPoint("/login")
            }
            oauth2ResourceServer {
                jwt {
                }
            }
        }
        return http.build()
    }

    @Bean
    @Order(4)
    fun defaultSecurityFilterChain(
        http: HttpSecurity,
        rf: RoleHierarchyAuthorizationManagerFactory,
    ): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/error", permitAll)
                authorize("/login", permitAll)
                authorize(GET, "/.well-known/**", permitAll)
                authorize(GET, "/nodeinfo/2.0", permitAll)

                authorize(POST, "/inbox", permitAll)
                authorize(POST, "/users/*/inbox", permitAll)
                authorize(GET, "/users/*", permitAll)
                authorize(GET, "/users/*/posts/*", permitAll)

                authorize(POST, "/api/v1/apps", permitAll)
                authorize(GET, "/api/v1/instance/**", permitAll)
                authorize(POST, "/api/v1/accounts", permitAll)

                authorize("/auth/sign_up", hasRole("ANONYMOUS"))
                authorize(GET, "/files/*", permitAll)
                authorize(GET, "/users/*/icon.jpg", permitAll)
                authorize(GET, "/users/*/header.jpg", permitAll)

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

                authorize(anyRequest, authenticated)
            }

            oauth2ResourceServer {
                jwt { }
            }

            formLogin {
            }

            csrf {
                ignoringRequestMatchers("/users/*/inbox", "/inbox", "/api/v1/apps")
            }

            headers {
                frameOptions {
                    sameOrigin = true
                }
            }
        }
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    @ConditionalOnProperty(name = ["hideout.security.jwt.generate"], havingValue = "false", matchIfMissing = true)
    fun genJwkSource(): JWKSource<SecurityContext> {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val generateKeyPair = keyPairGenerator.generateKeyPair()
        val rsaPublicKey = generateKeyPair.public as RSAPublicKey
        val rsaPrivateKey = generateKeyPair.private as RSAPrivateKey
        val rsaKey = RSAKey.Builder(rsaPublicKey).privateKey(rsaPrivateKey).keyID(UUID.randomUUID().toString()).build()

        val jwkSet = JWKSet(rsaKey)
        return ImmutableJWKSet(jwkSet)
    }

    @Bean
    @ConditionalOnProperty(name = ["hideout.security.jwt.generate"], havingValue = "")
    fun loadJwkSource(jwkConfig: JwkConfig): JWKSource<SecurityContext> {
        val rsaKey = RSAKey.Builder(RsaUtil.decodeRsaPublicKey(jwkConfig.publicKey))
            .privateKey(RsaUtil.decodeRsaPrivateKey(jwkConfig.privateKey)).keyID(jwkConfig.keyId).build()
        return ImmutableJWKSet(JWKSet(rsaKey))
    }

    @Bean
    fun jwtDecoder(jwkSource: JWKSource<SecurityContext>): JwtDecoder =
        OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource)

    @Bean
    fun authorizationServerSettings(): AuthorizationServerSettings {
        return AuthorizationServerSettings.builder().authorizationEndpoint("/oauth/authorize")
            .tokenEndpoint("/oauth/token").tokenRevocationEndpoint("/oauth/revoke").build()
    }

    @Bean
    fun jwtTokenCustomizer(): OAuth2TokenCustomizer<JwtEncodingContext> {
        return OAuth2TokenCustomizer { context: JwtEncodingContext ->

            if (OAuth2TokenType.ACCESS_TOKEN == context.tokenType &&
                context.authorization?.authorizationGrantType == AuthorizationGrantType.AUTHORIZATION_CODE
            ) {
                val userDetailsImpl = context.getPrincipal<Authentication>().principal as UserDetailsImpl
                context.claims.claim("uid", userDetailsImpl.id.toString())
            }
        }
    }

    @Bean
    @Primary
    fun jackson2ObjectMapperBuilderCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer {
            it.serializationInclusion(JsonInclude.Include.ALWAYS).serializers()
        }
    }

    @Bean
    fun mappingJackson2HttpMessageConverter(): MappingJackson2HttpMessageConverter {
        val builder = Jackson2ObjectMapperBuilder().serializationInclusion(JsonInclude.Include.NON_NULL)
        return MappingJackson2HttpMessageConverter(builder.build())
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

    //    @Bean
    fun beanDefinitionRegistryPostProcessor(): BeanDefinitionRegistryPostProcessor {
        return BeanDefinitionRegistryPostProcessor { registry: BeanDefinitionRegistry ->
            registry.getBeanDefinition(AbstractSecurityWebApplicationInitializer.DEFAULT_FILTER_NAME).beanClassName =
                CompositeFilterChainProxy::class.java.name
        }
    }

    internal class CompositeFilterChainProxy(filters: List<Filter?>) : FilterChainProxy() {
        private val doFilterDelegate: Filter

        private val springSecurityFilterChain: FilterChainProxy

        init {
            this.doFilterDelegate = createDoFilterDelegate(filters)
            this.springSecurityFilterChain = findFilterChainProxy(filters)
        }

        override fun afterPropertiesSet() {
            springSecurityFilterChain.afterPropertiesSet()
        }

        @Throws(IOException::class, ServletException::class)
        override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
            doFilterDelegate.doFilter(request, response, chain)
        }

        override fun getFilters(url: String): List<Filter> {
            return springSecurityFilterChain.getFilters(url)
        }

        override fun getFilterChains(): List<SecurityFilterChain> {
            return springSecurityFilterChain.filterChains
        }

        override fun setSecurityContextHolderStrategy(securityContextHolderStrategy: SecurityContextHolderStrategy) {
            springSecurityFilterChain.setSecurityContextHolderStrategy(securityContextHolderStrategy)
        }

        override fun setFilterChainValidator(filterChainValidator: FilterChainValidator) {
            springSecurityFilterChain.setFilterChainValidator(filterChainValidator)
        }

        override fun setFilterChainDecorator(filterChainDecorator: FilterChainDecorator) {
            springSecurityFilterChain.setFilterChainDecorator(filterChainDecorator)
        }

        override fun setFirewall(firewall: HttpFirewall) {
            springSecurityFilterChain.setFirewall(firewall)
        }

        override fun setRequestRejectedHandler(requestRejectedHandler: RequestRejectedHandler) {
            springSecurityFilterChain.setRequestRejectedHandler(requestRejectedHandler)
        }

        companion object {
            private fun createDoFilterDelegate(filters: List<Filter?>): Filter {
                val delegate: CompositeFilter = CompositeFilter()
                delegate.setFilters(filters)
                return delegate
            }

            private fun findFilterChainProxy(filters: List<Filter?>): FilterChainProxy {
                for (filter in filters) {
                    if (filter is FilterChainProxy) {
                        return filter
                    }
                    if (filter is DebugFilter) {
                        return filter.filterChainProxy
                    }
                }
                throw IllegalStateException("Couldn't find FilterChainProxy in $filters")
            }
        }
    }
}

@ConfigurationProperties("hideout.security.jwt")
@ConditionalOnProperty(name = ["hideout.security.jwt.generate"], havingValue = "")
data class JwkConfig(
    val keyId: String,
    val publicKey: String,
    val privateKey: String,
)

@Configuration
class PostSecurityConfig(
    val auth: AuthenticationManagerBuilder,
    val daoAuthenticationProvider: DaoAuthenticationProvider,
    val httpSignatureAuthenticationProvider: PreAuthenticatedAuthenticationProvider,
) {

    @PostConstruct
    fun config() {
        auth.authenticationProvider(daoAuthenticationProvider)
        auth.authenticationProvider(httpSignatureAuthenticationProvider)
    }
}
