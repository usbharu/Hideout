package dev.usbharu.hideout.activitypub.service.objects.emoji

import dev.usbharu.hideout.activitypub.domain.model.Emoji
import dev.usbharu.hideout.activitypub.service.common.APResourceResolveServiceImpl
import dev.usbharu.hideout.activitypub.service.common.resolve
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmoji
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiRepository
import dev.usbharu.hideout.core.service.instance.InstanceService
import dev.usbharu.hideout.core.service.media.MediaService
import dev.usbharu.hideout.core.service.media.RemoteMedia
import org.springframework.stereotype.Service
import java.net.URL
import java.time.Instant

@Service
class EmojiServiceImpl(
    private val customEmojiRepository: CustomEmojiRepository,
    private val instanceService: InstanceService,
    private val mediaService: MediaService,
    private val apResourceResolveServiceImpl: APResourceResolveServiceImpl,
    private val applicationConfig: ApplicationConfig
) : EmojiService {
    override suspend fun fetchEmoji(url: String): Pair<Emoji, CustomEmoji> {
        val emoji = apResourceResolveServiceImpl.resolve<Emoji>(url, null as Long?)
        return fetchEmoji(emoji)
    }

    override suspend fun fetchEmoji(emoji: Emoji): Pair<Emoji, CustomEmoji> = emoji to save(emoji)

    private suspend fun save(emoji: Emoji): CustomEmoji {
        val domain = URL(emoji.id).host
        val name = emoji.name.trim(':')
        val customEmoji = customEmojiRepository.findByNameAndDomain(name, domain)

        if (customEmoji != null) {
            return customEmoji
        }

        val instance = instanceService.fetchInstance(emoji.id)

        val media = mediaService.uploadRemoteMedia(
            RemoteMedia(
                emoji.name,
                emoji.icon.url,
                emoji.icon.mediaType.orEmpty(),
                null
            )
        )

        val customEmoji1 = CustomEmoji(
            id = customEmojiRepository.generateId(),
            name = name,
            domain = domain,
            instanceId = instance.id,
            url = media.url,
            category = null,
            createdAt = Instant.now()
        )

        return customEmojiRepository.save(customEmoji1)
    }

    override suspend fun findByEmojiName(emojiName: String): CustomEmoji? {
        val split = emojiName.trim(':').split("@")

        return when (split.size) {
            1 -> {
                customEmojiRepository.findByNameAndDomain(split.first(), applicationConfig.url.host)
            }

            2 -> {
                customEmojiRepository.findByNameAndDomain(split.first(), split[1])
            }

            else -> throw IllegalArgumentException("Unknown Emoji Format. $emojiName")
        }
    }
}
