package dev.usbharu.hideout.service.auth

import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.core.Transaction
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(private val userQueryService: UserQueryService, private val transaction: Transaction) :
    UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails = runBlocking {
        if (username == null) {
            throw UsernameNotFoundException("$username not found")
        }
        transaction.transaction {
            val findById = userQueryService.findByNameAndDomain(username, "")
            User(
                findById.name,
                findById.password,
                emptyList()
            )
        }
    }
}
