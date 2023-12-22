package dev.usbharu.hideout.activitypub.service.objects.emoji

import dev.usbharu.hideout.activitypub.domain.model.Emoji
import dev.usbharu.hideout.activitypub.service.common.APResourceResolveServiceImpl
import dev.usbharu.hideout.activitypub.service.common.resolve
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
    private val apResourceResolveServiceImpl: APResourceResolveServiceImpl
) : EmojiService {
    override suspend fun fetchEmoji(url: String): Pair<Emoji, CustomEmoji> {
        val emoji = apResourceResolveServiceImpl.resolve<Emoji>(url, null as Long?)
        return fetchEmoji(emoji)
    }

    override suspend fun fetchEmoji(emoji: Emoji): Pair<Emoji, CustomEmoji> {
        return emoji to save(emoji)
    }

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
            customEmojiRepository.generateId(),
            name,
            domain,
            instance.id,
            media.url,
            null,
            Instant.now()
        )

        return customEmojiRepository.save(customEmoji1)
    }
}
