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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.toPath

class ApatcheTikaFileTypeDeterminationServiceTest {
    @Test
    fun png() {
        val apatcheTikaFileTypeDeterminationService = ApatcheTikaFileTypeDeterminationService()

        val mimeType = apatcheTikaFileTypeDeterminationService.fileType(
            String.javaClass.classLoader.getResource("400x400.png").toURI().toPath(), "400x400.png"
        )

        assertThat(mimeType.type).isEqualTo("image")
        assertThat(mimeType.subtype).isEqualTo("png")
    }
}
