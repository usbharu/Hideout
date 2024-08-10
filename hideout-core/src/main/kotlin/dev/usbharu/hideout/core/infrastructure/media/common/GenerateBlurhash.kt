package dev.usbharu.hideout.core.infrastructure.media.common

import java.awt.image.BufferedImage

interface GenerateBlurhash {
    fun generateBlurhash(bufferedImage: BufferedImage): String
}
