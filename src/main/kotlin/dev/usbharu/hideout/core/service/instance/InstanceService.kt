package dev.usbharu.hideout.core.service.instance

import dev.usbharu.hideout.core.domain.model.instance.Instance
import dev.usbharu.hideout.core.domain.model.instance.InstanceRepository
import org.springframework.stereotype.Service
import java.time.Instant

interface InstanceService {
    suspend fun createNewInstance(instanceCreateDto: InstanceCreateDto): Instance
}


@Service
class InstanceServiceImpl(private val instanceRepository: InstanceRepository) : InstanceService {
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
