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

package dev.usbharu.hideout.core.service.media.converter.movie

import dev.usbharu.hideout.core.service.media.FileType
import dev.usbharu.hideout.core.service.media.MimeType
import dev.usbharu.hideout.core.service.media.ProcessedMedia
import dev.usbharu.hideout.core.service.media.ProcessedMediaPath
import dev.usbharu.hideout.core.service.media.converter.MediaProcessService
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.javacv.FFmpegFrameFilter
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Java2DFrameConverter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.min

@Service
@Qualifier("video")
class MovieMediaProcessService : MediaProcessService {
    override fun isSupport(mimeType: MimeType): Boolean = mimeType.type == "video"

    override suspend fun process(
        fileType: FileType,
        contentType: String,
        fileName: String,
        file: ByteArray,
        thumbnail: ByteArray?
    ): ProcessedMedia {
        TODO("Not yet implemented")
    }

    @Suppress("LongMethod", "NestedBlockDepth", "CognitiveComplexMethod")
    override suspend fun process(
        mimeType: MimeType,
        fileName: String,
        filePath: Path,
        thumbnails: Path?
    ): ProcessedMediaPath {
        val tempFile = Files.createTempFile("hideout-movie-processor-", ".tmp")
        val thumbnailFile = Files.createTempFile("hideout-movie-thumbnail-generate-", ".tmp")
        logger.info("START Convert Movie Media {}", fileName)
        FFmpegFrameGrabber(filePath.toFile()).use { grabber ->
            grabber.start()
            val width = grabber.imageWidth
            val height = grabber.imageHeight
            val frameRate = 60.0

            logger.debug("Movie Media Width {}, Height {}", width, height)

            FFmpegFrameFilter(
                "fps=fps=${frameRate.toInt()}",
                "anull",
                width,
                height,
                grabber.audioChannels
            ).use { filter ->

                filter.sampleFormat = grabber.sampleFormat
                filter.sampleRate = grabber.sampleRate
                filter.pixelFormat = grabber.pixelFormat
                filter.frameRate = grabber.frameRate
                filter.start()

                val videoBitRate = min(1300000, (width * height * frameRate * 1 * 0.07).toInt())

                logger.debug("Movie Media BitRate {}", videoBitRate)

                FFmpegFrameRecorder(tempFile.toFile(), width, height, grabber.audioChannels).use {
                    it.sampleRate = grabber.sampleRate
                    it.format = "mp4"
                    it.videoCodec = avcodec.AV_CODEC_ID_H264
                    it.audioCodec = avcodec.AV_CODEC_ID_AAC
                    it.audioChannels = grabber.audioChannels
                    it.videoQuality = 1.0
                    it.frameRate = frameRate
                    it.setVideoOption("preset", "ultrafast")
                    it.timestamp = 0
                    it.gopSize = frameRate.toInt()
                    it.videoBitrate = videoBitRate
                    it.start()

                    var bufferedImage: BufferedImage? = null

                    val frameConverter = Java2DFrameConverter()

                    while (true) {
                        val grab = grabber.grab() ?: break

                        if (bufferedImage == null) {
                            bufferedImage = frameConverter.convert(grab)
                        }

                        if (grab.image != null || grab.samples != null) {
                            filter.push(grab)
                        }
                        while (true) {
                            val frame = filter.pull() ?: break
                            it.record(frame)
                        }
                    }

                    if (bufferedImage != null) {
                        ImageIO.write(bufferedImage, "jpeg", thumbnailFile.toFile())
                    }
                }
            }
        }

        logger.info("SUCCESS Convert Movie Media {}", fileName)

        return ProcessedMediaPath(
            tempFile,
            thumbnailFile,
            MimeType("video", "mp4", FileType.Video),
            MimeType("image", "jpeg", FileType.Image)
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MovieMediaProcessService::class.java)
    }
}
