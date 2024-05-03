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

interface MediaFileRenameService {
    /**
     * メディアをリネームします
     *
     * @param uploadName アップロードされた時点でのファイル名
     * @param uploadMimeType アップロードされた時点でのMimeType
     * @param processedName 処理後のファイル名
     * @param processedMimeType 処理後のMimeType
     * @return リネーム後のファイル名
     */
    fun rename(uploadName: String, uploadMimeType: MimeType, processedName: String, processedMimeType: MimeType): String
}
