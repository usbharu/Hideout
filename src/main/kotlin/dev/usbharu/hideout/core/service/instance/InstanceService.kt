package dev.usbharu.hideout.core.service.instance

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.activitypub.domain.model.nodeinfo.Nodeinfo2_0
import dev.usbharu.hideout.core.domain.model.instance.Instance
import dev.usbharu.hideout.core.domain.model.instance.InstanceRepository
import dev.usbharu.hideout.core.domain.model.instance.Nodeinfo
import dev.usbharu.hideout.core.service.resource.ResourceResolveService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.net.URL
import java.time.Instant

interface InstanceService {
    suspend fun fetchInstance(url: String): Instance
    suspend fun createNewInstance(instanceCreateDto: InstanceCreateDto): Instance
}


@Service
class InstanceServiceImpl(
    private val instanceRepository: InstanceRepository,
    private val resourceResolveService: ResourceResolveService,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper
) : InstanceService {
    override suspend fun fetchInstance(url: String): Instance {
        val u = URL(url)
        val resolveInstanceUrl = u.protocol + "://" + u.host
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
                        nodeinfo20.metadata.nodeName,
                        nodeinfo20.metadata.nodeDescription,
                        resolveInstanceUrl,
                        resolveInstanceUrl + "/favicon.ico",
                        null,
                        nodeinfo20.software.name,
                        nodeinfo20.software.version
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
            instanceRepository.generateId(),
            instanceCreateDto.name ?: instanceCreateDto.url,
            instanceCreateDto.description ?: "",
            instanceCreateDto.url,
            instanceCreateDto.iconUrl,
            instanceCreateDto.sharedInbox,
            instanceCreateDto.software ?: "unknown",
            instanceCreateDto.version ?: "unknown",
            false,
            false,
            "",
            Instant.now()
        )
        instanceRepository.save(instance)
        return instance
    }
}
