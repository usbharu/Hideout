package dev.usbharu.hideout.activitypub.service.webfinger

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.user.User
import dev.usbharu.hideout.core.query.UserQueryService
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
