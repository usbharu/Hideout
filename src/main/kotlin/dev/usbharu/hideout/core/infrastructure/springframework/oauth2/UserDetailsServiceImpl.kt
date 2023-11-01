package dev.usbharu.hideout.core.infrastructure.springframework.oauth2

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.query.UserQueryService
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userQueryService: UserQueryService,
    private val applicationConfig: ApplicationConfig,
    private val transaction: Transaction
) :
    UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails = runBlocking {
        if (username == null) {
            throw UsernameNotFoundException("$username not found")
        }
        transaction.transaction {
            val findById = userQueryService.findByNameAndDomain(username, applicationConfig.url.host)
            UserDetailsImpl(
                id = findById.id,
                username = findById.name,
                password = findById.password,
                enabled = true,
                accountNonExpired = true,
                credentialsNonExpired = true,
                accountNonLocked = true,
                authorities = mutableListOf()
            )
        }
    }
}
