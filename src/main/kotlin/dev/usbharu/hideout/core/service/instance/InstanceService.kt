package dev.usbharu.hideout.core.service.instance

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.instance.Instance
import dev.usbharu.hideout.core.domain.model.instance.InstanceRepository
import dev.usbharu.hideout.core.domain.model.instance.Nodeinfo
import dev.usbharu.hideout.core.domain.model.instance.Nodeinfo2_0
import dev.usbharu.hideout.core.query.InstanceQueryService
import dev.usbharu.hideout.core.service.resource.ResourceResolveService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.net.URL
import java.time.Instant

interface InstanceService {
    suspend fun fetchInstance(url: String, sharedInbox: String? = null): Instance
    suspend fun createNewInstance(instanceCreateDto: InstanceCreateDto): Instance
}

@Service
class InstanceServiceImpl(
    private val instanceRepository: InstanceRepository,
    private val resourceResolveService: ResourceResolveService,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper,
    private val instanceQueryService: InstanceQueryService
) : InstanceService {
    override suspend fun fetchInstance(url: String, sharedInbox: String?): Instance {
        val u = URL(url)
        val resolveInstanceUrl = u.protocol + "://" + u.host

        try {
            return instanceQueryService.findByUrl(url)
        } catch (e: FailedToGetResourcesException) {
            logger.info("Instance not found. try fetch instance info. url: {}", resolveInstanceUrl)
            logger.debug("Failed to get resources. url: {}", resolveInstanceUrl, e)
        }

        val nodeinfoJson = resourceResolveService.resolve("$resolveInstanceUrl/.well-known/nodeinfo").bodyAsText()
        val nodeinfo = objectMapper.readValue(nodeinfoJson, Nodeinfo::class.java)
        val nodeinfoPathMap = nodeinfo.links.associate { it.rel to it.href }

        for ((key, value) in nodeinfoPathMap) {
            when (key) {
                "http://nodeinfo.diaspora.software/ns/schema/2.0" -> {
                    val nodeinfo20 = objectMapper.readValue(
                        resourceResolveService.resolve(value!!).bodyAsText(),
                        Nodeinfo2_0::class.java
                    )

                    val instanceCreateDto = InstanceCreateDto(
                        name = nodeinfo20.metadata?.nodeName,
                        description = nodeinfo20.metadata?.nodeDescription,
                        url = resolveInstanceUrl,
                        iconUrl = resolveInstanceUrl + "/favicon.ico",
                        sharedInbox = sharedInbox,
                        software = nodeinfo20.software?.name,
                        version = nodeinfo20.software?.version
                    )
                    return createNewInstance(instanceCreateDto)
                }

                // TODO: 多分2.0と2.1で互換性有るのでそのまま使うけどなおす
                "http://nodeinfo.diaspora.software/ns/schema/2.1" -> {
                    val nodeinfo20 = objectMapper.readValue(
                        resourceResolveService.resolve(value!!).bodyAsText(),
                        Nodeinfo2_0::class.java
                    )

                    val instanceCreateDto = InstanceCreateDto(
                        name = nodeinfo20.metadata?.nodeName,
                        description = nodeinfo20.metadata?.nodeDescription,
                        url = resolveInstanceUrl,
                        iconUrl = resolveInstanceUrl + "/favicon.ico",
                        sharedInbox = sharedInbox,
                        software = nodeinfo20.software?.name,
                        version = nodeinfo20.software?.version
                    )
                    return createNewInstance(instanceCreateDto)
                }

                else -> {
                    TODO()
                }
            }
        }

        throw IllegalStateException("Nodeinfo aren't found.")
    }

    override suspend fun createNewInstance(instanceCreateDto: InstanceCreateDto): Instance {
        val instance = Instance(
            id = instanceRepository.generateId(),
            name = instanceCreateDto.name ?: instanceCreateDto.url,
            description = instanceCreateDto.description.orEmpty(),
            url = instanceCreateDto.url,
            iconUrl = instanceCreateDto.iconUrl,
            sharedInbox = instanceCreateDto.sharedInbox,
            software = instanceCreateDto.software ?: "unknown",
            version = instanceCreateDto.version ?: "unknown",
            isBlocked = false,
            isMuted = false,
            moderationNote = "",
            createdAt = Instant.now()
        )
        instanceRepository.save(instance)
        return instance
    }

    companion object {
        private val logger = LoggerFactory.getLogger(InstanceServiceImpl::class.java)
    }
}
