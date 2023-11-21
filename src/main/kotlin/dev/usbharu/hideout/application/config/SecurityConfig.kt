package dev.usbharu.hideout.application.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.infrastructure.springframework.httpsignature.HttpSignatureFilter
import dev.usbharu.hideout.core.infrastructure.springframework.httpsignature.HttpSignatureUserDetailsService
import dev.usbharu.hideout.core.infrastructure.springframework.httpsignature.HttpSignatureVerifierComposite
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.UserDetailsImpl
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.util.RsaUtil
import dev.usbharu.httpsignature.sign.RsaSha256HttpSignatureSigner
import dev.usbharu.httpsignature.verify.DefaultSignatureHeaderParser
import dev.usbharu.httpsignature.verify.RsaSha256HttpSignatureVerifier
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.authentication.AccountStatusUserDetailsChecker
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.ExceptionTranslationFilter
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher
import org.springframework.security.web.util.matcher.AnyRequestMatcher
import org.springframework.web.servlet.handler.HandlerMappingIntrospector
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*

@EnableWebSecurity(debug = false)
@Configuration
@Suppress("FunctionMaxLength", "TooManyFunctions")
class SecurityConfig {

    @Autowired
    private lateinit var userQueryService: UserQueryService

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager? =
        authenticationConfiguration.authenticationManager

    @Bean
    @Order(1)
    fun httpSignatureFilterChain(
        http: HttpSecurity,
        httpSignatureFilter: HttpSignatureFilter,
        introspector: HandlerMappingIntrospector
    ): SecurityFilterChain {
        val builder = MvcRequestMatcher.Builder(introspector)
        http
            .securityMatcher("/inbox", "/outbox", "/users/*/inbox", "/users/*/outbox", "/users/*/posts/*")
            .addFilter(httpSignatureFilter)
            .addFilterBefore(
                ExceptionTranslationFilter(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)),
                HttpSignatureFilter::class.java
            )
            .authorizeHttpRequests {
                it.requestMatchers(
                    builder.pattern("/inbox"),
                    builder.pattern("/outbox"),
                    builder.pattern("/users/*/inbox"),
                    builder.pattern("/users/*/outbox")
                ).authenticated()
                it.anyRequest().permitAll()
            }
            .csrf {
                it.disable()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                it.defaultAuthenticationEntryPointFor(
                    HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    AnyRequestMatcher.INSTANCE
                )
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

        return http.build()
    }

    @Bean
    fun getHttpSignatureFilter(
        authenticationManager: AuthenticationManager,
        transaction: Transaction,
        apUserService: APUserService
    ): HttpSignatureFilter {
        val httpSignatureFilter = HttpSignatureFilter(DefaultSignatureHeaderParser(), transaction, apUserService)
        httpSignatureFilter.setAuthenticationManager(authenticationManager)
        httpSignatureFilter.setContinueFilterChainOnUnsuccessfulAuthentication(false)
        val authenticationEntryPointFailureHandler =
            AuthenticationEntryPointFailureHandler(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        authenticationEntryPointFailureHandler.setRethrowAuthenticationServiceException(false)
        httpSignatureFilter.setAuthenticationFailureHandler(authenticationEntryPointFailureHandler)
        return httpSignatureFilter
    }

    @Bean
    fun httpSignatureAuthenticationProvider(transaction: Transaction): PreAuthenticatedAuthenticationProvider {
        val provider = PreAuthenticatedAuthenticationProvider()
        provider.setPreAuthenticatedUserDetailsService(
            HttpSignatureUserDetailsService(
                userQueryService,
                HttpSignatureVerifierComposite(
                    mapOf(
                        "rsa-sha256" to RsaSha256HttpSignatureVerifier(
                            DefaultSignatureHeaderParser(), RsaSha256HttpSignatureSigner()
                        )
                    ),
                    DefaultSignatureHeaderParser()
                ),
                transaction
            )
        )
        provider.setUserDetailsChecker(AccountStatusUserDetailsChecker())
        return provider
    }

    @Bean
    @Order(2)
    fun oauth2SecurityFilterChain(http: HttpSecurity, introspector: HandlerMappingIntrospector): SecurityFilterChain {
        val builder = MvcRequestMatcher.Builder(introspector)

        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)
        http.exceptionHandling {
            it.authenticationEntryPoint(
                LoginUrlAuthenticationEntryPoint("/login")
            )
        }.oauth2ResourceServer {
            it.jwt(Customizer.withDefaults())
        }
        return http.build()
    }

    @Bean
    @Order(4)
    fun defaultSecurityFilterChain(http: HttpSecurity, introspector: HandlerMappingIntrospector): SecurityFilterChain {
        val builder = MvcRequestMatcher.Builder(introspector)

        http.authorizeHttpRequests {
            it.requestMatchers(PathRequest.toH2Console()).permitAll()
            it.requestMatchers(
                builder.pattern("/inbox"),
                builder.pattern("/users/*/inbox"),
                builder.pattern("/api/v1/apps"),
                builder.pattern("/api/v1/instance/**"),
                builder.pattern("/.well-known/**"),
                builder.pattern("/error"),
                builder.pattern("/nodeinfo/2.0")
            ).permitAll()
            it.requestMatchers(
                builder.pattern("/auth/**")
            ).anonymous()
            it.requestMatchers(builder.pattern("/change-password")).authenticated()
            it.requestMatchers(builder.pattern("/api/v1/accounts/verify_credentials"))
                .hasAnyAuthority("SCOPE_read", "SCOPE_read:accounts")
            it.anyRequest().permitAll()
        }
        http.oauth2ResourceServer {
            it.jwt(Customizer.withDefaults())
        }.passwordManagement { }.formLogin(Customizer.withDefaults()).csrf {
            it.ignoringRequestMatchers(builder.pattern("/users/*/inbox"))
            it.ignoringRequestMatchers(builder.pattern(HttpMethod.POST, "/api/v1/apps"))
            it.ignoringRequestMatchers(builder.pattern("/inbox"))
            it.ignoringRequestMatchers(PathRequest.toH2Console())
        }.headers {
            it.frameOptions {
                it.sameOrigin()
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
            if (OAuth2TokenType.ACCESS_TOKEN == context.tokenType) {
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
}

@ConfigurationProperties("hideout.security.jwt")
@ConditionalOnProperty(name = ["hideout.security.jwt.generate"], havingValue = "")
data class JwkConfig(
    val keyId: String,
    val publicKey: String,
    val privateKey: String
)
