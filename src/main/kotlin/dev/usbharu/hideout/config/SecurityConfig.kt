package dev.usbharu.hideout.config

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import dev.usbharu.hideout.domain.model.UserDetailsImpl
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
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
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher
import org.springframework.web.servlet.handler.HandlerMappingIntrospector
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*

@EnableWebSecurity(debug = false)
@Configuration
class SecurityConfig {

    @Bean
    @Order(1)
    fun oauth2SecurityFilterChain(http: HttpSecurity, introspector: HandlerMappingIntrospector): SecurityFilterChain {
        val builder = MvcRequestMatcher.Builder(introspector)

        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)
        http
            .exceptionHandling {
                it.authenticationEntryPoint(
                    LoginUrlAuthenticationEntryPoint("/login")
                )
            }
            .oauth2ResourceServer {
                it.jwt(Customizer.withDefaults())
            }
        return http.build()
    }

    @Bean
    @Order(2)
    fun defaultSecurityFilterChain(http: HttpSecurity, introspector: HandlerMappingIntrospector): SecurityFilterChain {
        val builder = MvcRequestMatcher.Builder(introspector)

        http
            .authorizeHttpRequests {
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
        http
            .oauth2ResourceServer {
                it.jwt(Customizer.withDefaults())
            }
            .passwordManagement { }
            .formLogin(Customizer.withDefaults())
            .csrf {
                it.ignoringRequestMatchers(builder.pattern("/users/*/inbox"))
                it.ignoringRequestMatchers(builder.pattern("/inbox"))
                it.ignoringRequestMatchers(PathRequest.toH2Console())
            }
            .headers {
                it.frameOptions {
                    it.sameOrigin()
                }
            }
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun genJwkSource(): JWKSource<SecurityContext> {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val generateKeyPair = keyPairGenerator.generateKeyPair()
        val rsaPublicKey = generateKeyPair.public as RSAPublicKey
        val rsaPrivateKey = generateKeyPair.private as RSAPrivateKey
        val rsaKey = RSAKey
            .Builder(rsaPublicKey)
            .privateKey(rsaPrivateKey)
            .keyID(UUID.randomUUID().toString())
            .build()

        val jwkSet = JWKSet(rsaKey)
        return ImmutableJWKSet(jwkSet)
    }

    @Bean
    @ConditionalOnProperty(name = ["hideout.security.jwt.generate"], havingValue = "")
    fun loadJwkSource(jwkConfig: JwkConfig): JWKSource<SecurityContext> {
        val rsaKey = RSAKey.Builder(jwkConfig.publicKey)
            .privateKey(jwkConfig.privateKey)
            .keyID(jwkConfig.keyId)
            .build()
        return ImmutableJWKSet(JWKSet(rsaKey))
    }

    @Bean
    fun jwtDecoder(jwkSource: JWKSource<SecurityContext>): JwtDecoder =
        OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource)

    @Bean
    fun authorizationServerSettings(): AuthorizationServerSettings {
        return AuthorizationServerSettings.builder()
            .authorizationEndpoint("/oauth/authorize")
            .tokenEndpoint("/oauth/token")
            .tokenRevocationEndpoint("/oauth/revoke")
            .build()
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
}

@ConfigurationProperties("hideout.security.jwt")
@ConditionalOnProperty(name = ["hideout.security.jwt.generate"], havingValue = "")
data class JwkConfig(
    val keyId: String,
    val publicKey: RSAPublicKey,
    val privateKey: RSAPrivateKey
)
