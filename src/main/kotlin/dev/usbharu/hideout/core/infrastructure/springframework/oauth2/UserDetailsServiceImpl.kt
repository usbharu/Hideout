package dev.usbharu.hideout.core.infrastructure.springframework.oauth2

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.query.ActorQueryService
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val actorQueryService: ActorQueryService,
    private val applicationConfig: ApplicationConfig,
    private val userDetailRepository: UserDetailRepository,
    private val transaction: Transaction
) :
    UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails = runBlocking {
        if (username == null) {
            throw UsernameNotFoundException("$username not found")
        }
        transaction.transaction {
            val findById = try {
                actorQueryService.findByNameAndDomain(username, applicationConfig.url.host)
            } catch (e: FailedToGetResourcesException) {
                throw UsernameNotFoundException("$username not found")
            }
            val userDetails = userDetailRepository.findByActorId(findById.id)
                ?: throw UsernameNotFoundException("${findById.id} not found.")
            UserDetailsImpl(
                id = findById.id,
                username = findById.name,
                password = userDetails.password,
                enabled = true,
                accountNonExpired = true,
                credentialsNonExpired = true,
                accountNonLocked = true,
                authorities = mutableListOf()
            )
        }
    }
}
