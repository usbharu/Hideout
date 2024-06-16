package dev.usbharu.hideout.core.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * メディアの保存にローカルファイルシステムを使用する際のコンフィグ
 *
 * @property path フォゾンする場所へのパス。 /から始めると絶対パスとなります。
 * @property publicUrl 公開用URL 省略可能 指定するとHideoutがファイルを配信しなくなります。
 */
@ConfigurationProperties("hideout.storage.local")
@ConditionalOnProperty("hideout.storage.type", havingValue = "local", matchIfMissing = true)
data class LocalStorageConfig(
    val path: String = "files",
    val publicUrl: String?
)