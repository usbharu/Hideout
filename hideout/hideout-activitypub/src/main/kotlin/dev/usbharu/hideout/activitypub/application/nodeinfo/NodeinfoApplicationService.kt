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
    @Autowired(required = false) private val buildInfo: BuildProperties?,
    private val applicationConfig: ApplicationConfig,
    transaction: Transaction,
) : AbstractApplicationService<NodeinfoRequest, Nodeinfo2_0>(
    transaction, logger
) {
    override suspend fun internalExecute(command: NodeinfoRequest, principal: Principal): Nodeinfo2_0 {
        return when (command.version) {
            "2.0", "2.1" -> Nodeinfo2_0(
                version = command.version,
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

            else -> throw IllegalArgumentException("Invalid command version ${command.version}")
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(NodeinfoApplicationService::class.java)
    }
}
