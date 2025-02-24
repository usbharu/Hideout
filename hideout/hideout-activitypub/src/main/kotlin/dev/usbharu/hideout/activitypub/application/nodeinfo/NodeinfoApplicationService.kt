package dev.usbharu.hideout.activitypub.application.nodeinfo

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.info.BuildProperties
import org.springframework.stereotype.Service

@Service/*
 * 今後ユーザーエージェントに応じてレスポンスを変更する可能性があるためApplicationServiceとして作成する commandは現時点ではUnitだが今後変化する可能性あり
 */
class NodeinfoApplicationService(
    @Autowired(required = false) private val buildInfo: BuildProperties? = null,
    private val applicationConfig: ApplicationConfig,
    transaction: Transaction,
) : AbstractApplicationService<Unit, Nodeinfo2_0>(
    transaction, logger
) {
    override suspend fun internalExecute(command: Unit, principal: Principal): Nodeinfo2_0 {
        return Nodeinfo2_0(
            version = "2.0",
            software = mapOf(
                "name" to "hideout", "version" to (buildInfo?.version ?: "UNKNOWN")
            ),
            protocol = listOf("activitypub"),
            NodeinfoUsage(
                users = mapOf(
                    "total" to 0, "activeMonth" to 0, "activeHalfyear" to 0
                ), localPosts = 0
            ),
            openRegistration = applicationConfig.private.not(),
            metadata = mapOf()
        )
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(NodeinfoApplicationService::class.java)
    }
}
