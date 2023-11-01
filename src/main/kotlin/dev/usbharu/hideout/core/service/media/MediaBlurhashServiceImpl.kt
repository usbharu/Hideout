package dev.usbharu.hideout.core.service.media

import io.trbl.blurhash.BlurHash
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage

@Service
class MediaBlurhashServiceImpl : MediaBlurhashService {
    override fun generateBlurhash(bufferedImage: BufferedImage): String = BlurHash.encode(bufferedImage)
}
