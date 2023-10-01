package dev.usbharu.hideout.config

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import java.time.Instant


@ReadingConverter
enum class InstantLongConverter : Converter<Long, Instant> {
    INSTANCE;

    override fun convert(source: Long): Instant? {
        return Instant.ofEpochMilli(source)
    }
}


@WritingConverter
enum class InstantJavaLongConverter : Converter<Instant, Long> {
    INSTANCE;

    override fun convert(source: Instant): Long? {
        return source.toEpochMilli()
    }
}
