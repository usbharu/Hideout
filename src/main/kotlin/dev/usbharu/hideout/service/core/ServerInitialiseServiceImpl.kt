package dev.usbharu.hideout.service.core

import dev.usbharu.hideout.domain.model.hideout.entity.Jwt
import dev.usbharu.hideout.domain.model.hideout.entity.Meta
import dev.usbharu.hideout.repository.MetaRepository
import dev.usbharu.hideout.util.ServerUtil
import org.koin.core.annotation.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.KeyPairGenerator
import java.util.*

@Single
@Service
class ServerInitialiseServiceImpl(
    private val metaRepository: MetaRepository,
    private val transaction: Transaction
) :
    ServerInitialiseService {

    val logger: Logger = LoggerFactory.getLogger(ServerInitialiseServiceImpl::class.java)

    override suspend fun init() {
        transaction.transaction {
            val savedMeta = metaRepository.get()
            val implementationVersion = ServerUtil.getImplementationVersion()
            if (wasInitialised(savedMeta).not()) {
                logger.info("Start Initialise")
                initialise(implementationVersion)
                logger.info("Finish Initialise")
                return@transaction
            }

            if (isVersionChanged(requireNotNull(savedMeta))) {
                logger.info("Version changed!! (${savedMeta.version} -> $implementationVersion)")
                updateVersion(savedMeta, implementationVersion)
            }
        }
    }

    private fun wasInitialised(meta: Meta?): Boolean {
        logger.debug("Initialise checking...")
        return meta != null
    }

    private fun isVersionChanged(meta: Meta): Boolean = meta.version != ServerUtil.getImplementationVersion()

    private suspend fun initialise(implementationVersion: String) {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val generateKeyPair = keyPairGenerator.generateKeyPair()
        val jwt = Jwt(
            UUID.randomUUID(),
            Base64.getEncoder().encodeToString(generateKeyPair.private.encoded),
            Base64.getEncoder().encodeToString(generateKeyPair.public.encoded)
        )
        val meta = Meta(implementationVersion, jwt)
        metaRepository.save(meta)
    }

    private suspend fun updateVersion(meta: Meta, version: String) {
        metaRepository.save(meta.copy(version = version))
    }
}
