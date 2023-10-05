package dev.usbharu.hideout.service.media

import dev.usbharu.hideout.domain.model.hideout.dto.ProcessedFile
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

@Service
class ThumbnailGenerateServiceImpl : ThumbnailGenerateService {
    override fun generate(bufferedImage: InputStream, width: Int, height: Int): ProcessedFile? {
        val image = ImageIO.read(bufferedImage)
        return internalGenerate(image)
    }

    override fun generate(outputStream: ByteArray, width: Int, height: Int): ProcessedFile? {
        val image = ImageIO.read(outputStream.inputStream())
        return internalGenerate(image)
    }

    private fun internalGenerate(image: BufferedImage): ProcessedFile {
        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(image, "jpeg", byteArrayOutputStream)
        return ProcessedFile(byteArrayOutputStream.toByteArray(), "jpg")
    }
}
