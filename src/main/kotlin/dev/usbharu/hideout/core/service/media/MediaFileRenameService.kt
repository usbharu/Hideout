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
