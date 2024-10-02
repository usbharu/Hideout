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

package dev.usbharu.owl.consumer

import java.nio.file.Files
import java.nio.file.Path
import java.util.*

/**
 * 単独で起動できるConsumerの構成のローダー
 */
object StandaloneConsumerConfigLoader {
    /**
     * [Path]から構成を読み込みます
     *
     * @param path 読み込むパス
     * @return 読み込まれた構成
     */
    fun load(path: Path): StandaloneConsumerConfig {
        val properties = Properties()

        properties.load(Files.newInputStream(path))

        val address = properties.getProperty("address")
        val port = properties.getProperty("port").toInt()
        val name = properties.getProperty("name")
        val hostname = properties.getProperty("hostname")
        val concurrency = properties.getProperty("concurrency").toInt()

        return StandaloneConsumerConfig(address, port, name, hostname, concurrency)
    }
}
