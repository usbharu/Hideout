package dev.usbharu.hideout.service.media

import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.imageio.ImageIO
import javax.imageio.stream.MemoryCacheImageOutputStream

@Service
class ThumbnailGenerateServiceImpl : ThumbnailGenerateService {
    override fun generate(bufferedImage: InputStream, width: Int, height: Int): ByteArrayOutputStream {
        val image = ImageIO.read(bufferedImage)
        return internalGenerate(image)
    }

    override fun generate(outputStream: OutputStream, width: Int, height: Int): ByteArrayOutputStream {
        val image = ImageIO.read(MemoryCacheImageOutputStream(outputStream))
        return internalGenerate(image)
    }

    private fun internalGenerate(image: BufferedImage): ByteArrayOutputStream {
        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(image, "webp", byteArrayOutputStream)
        return byteArrayOutputStream
    }
}
