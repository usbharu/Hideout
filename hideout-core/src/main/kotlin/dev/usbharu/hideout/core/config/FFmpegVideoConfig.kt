package dev.usbharu.hideout.core.config

import org.bytedeco.ffmpeg.global.avcodec
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("hideout.media.video.ffmpeg")
data class FFmpegVideoConfig(
    val frameRate: Int = 60,
    val maxWidth: Int = 1920,
    val maxHeight: Int = 1080,
    val format: String = "mp4",
    val videoCodec: Int = avcodec.AV_CODEC_ID_H264,
    val audioCodec: Int = avcodec.AV_CODEC_ID_AAC,
    val videoQuality: Double = 1.0,
    val videoOption: List<String> = listOf("preset", "ultrafast"),
    val maxBitrate: Int = 1300000,
)