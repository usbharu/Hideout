package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Instance
import dev.usbharu.hideout.core.infrastructure.exposedrepository.toInstance
import dev.usbharu.hideout.core.query.InstanceQueryService
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import dev.usbharu.hideout.core.domain.model.instance.Instance as InstanceEntity

@Repository
class InstanceQueryServiceImpl : InstanceQueryService {
    override suspend fun findByUrl(url: String): InstanceEntity = Instance.select { Instance.url eq url }
        .singleOr { FailedToGetResourcesException("url is doesn't exist") }.toInstance()
}
