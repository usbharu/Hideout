package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.infrastructure.exposedrepository.AbstractRepository
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Actors
import dev.usbharu.hideout.core.infrastructure.exposedrepository.UserDetails
import dev.usbharu.hideout.core.query.principal.PrincipalDTO
import dev.usbharu.hideout.core.query.principal.PrincipalQueryService
import org.jetbrains.exposed.sql.selectAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExposedPrincipalQueryService : PrincipalQueryService, AbstractRepository() {
    override suspend fun findByUserDetailId(userDetailId: UserDetailId): PrincipalDTO {
        return query {
            UserDetails.leftJoin(Actors).selectAll().where { UserDetails.id eq userDetailId.id }.single()
                .let {
                    PrincipalDTO(
                        UserDetailId(it[UserDetails.id]),
                        ActorId(it[UserDetails.actorId]),
                        it[Actors.name],
                        it[Actors.domain]
                    )
                }
        }
    }

    override val logger: Logger
        get() = Companion.logger

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ExposedPrincipalQueryService::class.java)
    }
}