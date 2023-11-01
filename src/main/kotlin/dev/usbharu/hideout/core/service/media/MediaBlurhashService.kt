package dev.usbharu.hideout.core.service.media

import java.awt.image.BufferedImage

interface MediaBlurhashService {
    fun generateBlurhash(bufferedImage: BufferedImage): String
}
