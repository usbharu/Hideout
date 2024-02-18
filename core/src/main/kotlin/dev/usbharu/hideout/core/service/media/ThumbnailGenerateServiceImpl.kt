/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.core.service.media

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
