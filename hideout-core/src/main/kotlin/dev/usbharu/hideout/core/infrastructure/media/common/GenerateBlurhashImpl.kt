package dev.usbharu.hideout.core.infrastructure.media.common

import io.trbl.blurhash.BlurHash
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage

@Component
class GenerateBlurhashImpl : GenerateBlurhash {
    override fun generateBlurhash(bufferedImage: BufferedImage): String {
        return BlurHash.encode(bufferedImage)
    }
}