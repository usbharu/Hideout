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

/**
 * メディアを保存するインタフェース
 *
 */
interface MediaDataStore {
    /**
     * InputStreamを使用してメディアを保存します
     *
     * @param dataMediaSave FileとThumbnailのinputStream
     * @return 保存されたメディア
     */
    suspend fun save(dataMediaSave: MediaSave): SavedMedia

    /**
     * 一時ファイルのパスを使用してメディアを保存します
     *
     * @param dataSaveRequest FileとThumbnailのパス
     * @return 保存されたメディア
     */
    suspend fun save(dataSaveRequest: MediaSaveRequest): SavedMedia

    /**
     * メディアを削除します
     * 実装はサムネイル、メタデータなども削除するべきです。
     *
     * @param id 削除するメディアのid 通常は[SuccessSavedMedia.name]を指定します。
     */
    suspend fun delete(id: String)
}
