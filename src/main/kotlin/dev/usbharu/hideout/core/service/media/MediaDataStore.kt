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
