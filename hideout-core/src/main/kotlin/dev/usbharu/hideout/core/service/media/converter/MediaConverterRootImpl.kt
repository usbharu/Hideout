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

package dev.usbharu.hideout.core.service.media.converter

import dev.usbharu.hideout.core.service.media.FileType
import dev.usbharu.hideout.core.service.media.ProcessedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class MediaConverterRootImpl(private val converters: List<MediaConverter>) : MediaConverterRoot {
    override suspend fun convert(
        fileType: FileType,
        contentType: String,
        filename: String,
        inputStream: InputStream
    ): ProcessedFile {
        val convert = converters.find {
            it.isSupport(fileType)
        }?.convert(inputStream)
        if (convert != null) {
            return convert
        }
        return withContext(Dispatchers.IO) {
            if (filename.contains('.')) {
                ProcessedFile(inputStream.readAllBytes(), filename.substringAfterLast("."))
            } else {
                ProcessedFile(inputStream.readAllBytes(), contentType.substringAfterLast("/"))
            }
        }
    }
}
