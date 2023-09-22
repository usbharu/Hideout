package dev.usbharu.hideout.service.api

import dev.usbharu.hideout.domain.model.hideout.entity.User
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.core.Transaction
import org.springframework.stereotype.Service

@Service
interface WebFingerApiService {
    suspend fun findByNameAndDomain(name: String, domain: String): User
}

@Service
class WebFingerApiServiceImpl(private val transaction: Transaction, private val userQueryService: UserQueryService) :
    WebFingerApiService {
    override suspend fun findByNameAndDomain(name: String, domain: String): User {
        return transaction.transaction {
            userQueryService.findByNameAndDomain(name, domain)
        }
    }
}
