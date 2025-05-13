package dev.usbharu.hideout.core.infrastructure.springframework

import org.springframework.context.support.AbstractMessageSource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.text.MessageFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MergedPropertiesMessageSource : AbstractMessageSource() {

    private val messages: MutableMap<Locale, Properties> = ConcurrentHashMap()

    init {
        loadAllLocaleProperties("classpath*:/messages/hideout-web-messages*.properties")
    }

    private fun loadAllLocaleProperties(locationPattern: String) {
        val resolver = PathMatchingResourcePatternResolver()
        val resources = resolver.getResources(locationPattern)

        for (resource in resources) {
            val filename = resource.filename ?: continue

            val localeSuffix = filename
                .removePrefix("hideout-web-messages")
                .removeSuffix(".properties")
                .takeIf { it.isNotBlank() }
                ?: "default"

            val locale = if (localeSuffix == "default") {
                Locale.ROOT
            } else {
                Locale.forLanguageTag(localeSuffix.replace('_', '-'))
            }

            val props = messages.getOrPut(locale) { Properties() }

            resource.inputStream.use { stream ->
                InputStreamReader(stream, StandardCharsets.UTF_8).use { reader ->
                    val newProps = Properties()
                    newProps.load(reader)
                    props.putAll(newProps) // 上書きあり
                }
            }
        }
    }

    override fun resolveCode(code: String, locale: Locale): MessageFormat? {
        val props = messages[locale] ?: messages[Locale.ROOT] ?: return null
        val msg = props.getProperty(code) ?: return null
        return MessageFormat(msg, locale)
    }

    override fun resolveCodeWithoutArguments(code: String, locale: Locale): String? {
        val props = messages[locale] ?: messages[Locale.ROOT] ?: return null
        return props.getProperty(code)
    }
}