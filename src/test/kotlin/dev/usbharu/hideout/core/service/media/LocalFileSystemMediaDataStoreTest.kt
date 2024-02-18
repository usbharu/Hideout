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

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.config.LocalStorageConfig
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URL
import java.nio.file.Path
import java.util.*
import kotlin.io.path.readBytes
import kotlin.io.path.toPath

class LocalFileSystemMediaDataStoreTest {

    private val path = String.javaClass.classLoader.getResource("400x400.png")?.toURI()?.toPath()!!

    @Test
    fun `save inputStreamを使用して正常に保存できる`() = runTest {
        val applicationConfig = ApplicationConfig(URL("https://example.com"))
        val storageConfig = LocalStorageConfig("files", null)

        val localFileSystemMediaDataStore = LocalFileSystemMediaDataStore(applicationConfig, storageConfig)

        val fileInputStream = path.readBytes()

        assertThat(fileInputStream.size).isNotEqualTo(0)

        val mediaSave = MediaSave(
            "test-media-1${UUID.randomUUID()}.png",
            "",
            fileInputStream,
            fileInputStream
        )

        val save = localFileSystemMediaDataStore.save(mediaSave)

        assertThat(save).isInstanceOf(SuccessSavedMedia::class.java)

        save as SuccessSavedMedia

        assertThat(Path.of("files").toAbsolutePath().resolve(save.name))
            .exists()
            .hasSize(fileInputStream.size.toLong())
        assertThat(Path.of("files").toAbsolutePath().resolve("thumbnail-" + save.name))
            .exists()
            .hasSize(fileInputStream.size.toLong())
    }

    @Test
    fun 一時ファイルを使用して正常に保存できる() = runTest {
        val applicationConfig = ApplicationConfig(URL("https://example.com"))
        val storageConfig = LocalStorageConfig("files", null)

        val localFileSystemMediaDataStore = LocalFileSystemMediaDataStore(applicationConfig, storageConfig)

        val fileInputStream = path.readBytes()

        assertThat(fileInputStream.size).isNotEqualTo(0)

        val saveRequest = MediaSaveRequest(
            "test-media-2${UUID.randomUUID()}.png",
            "",
            path,
            path
        )

        val save = localFileSystemMediaDataStore.save(saveRequest)

        assertThat(save).isInstanceOf(SuccessSavedMedia::class.java)

        save as SuccessSavedMedia

        assertThat(Path.of("files").toAbsolutePath().resolve(save.name))
            .exists()
            .hasSize(fileInputStream.size.toLong())
        assertThat(Path.of("files").toAbsolutePath().resolve("thumbnail-" + save.name))
            .exists()
            .hasSize(fileInputStream.size.toLong())
    }

    @Test
    fun idを使用して削除できる() = runTest {
        val applicationConfig = ApplicationConfig(URL("https://example.com"))
        val storageConfig = LocalStorageConfig("files", null)

        val localFileSystemMediaDataStore = LocalFileSystemMediaDataStore(applicationConfig, storageConfig)

        val fileInputStream = path.readBytes()

        assertThat(fileInputStream.size).isNotEqualTo(0)

        val saveRequest = MediaSaveRequest(
            "test-media-2${UUID.randomUUID()}.png",
            "",
            path,
            path
        )

        val save = localFileSystemMediaDataStore.save(saveRequest)

        assertThat(save).isInstanceOf(SuccessSavedMedia::class.java)

        save as SuccessSavedMedia

        assertThat(Path.of("files").toAbsolutePath().resolve(save.name))
            .exists()
            .hasSize(fileInputStream.size.toLong())
        assertThat(Path.of("files").toAbsolutePath().resolve("thumbnail-" + save.name))
            .exists()
            .hasSize(fileInputStream.size.toLong())


        localFileSystemMediaDataStore.delete(save.name)

        assertThat(Path.of("files").toAbsolutePath().resolve(save.name))
            .doesNotExist()
        assertThat(Path.of("files").toAbsolutePath().resolve("thumbnail-" + save.name))
            .doesNotExist()
    }
}
