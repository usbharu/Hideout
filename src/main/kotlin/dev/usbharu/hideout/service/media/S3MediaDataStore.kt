package dev.usbharu.hideout.service.media

import dev.usbharu.hideout.config.StorageConfig
import dev.usbharu.hideout.domain.model.MediaSave
import dev.usbharu.hideout.domain.model.hideout.dto.SavedMedia
import dev.usbharu.hideout.domain.model.hideout.dto.SuccessSavedMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetUrlRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Service
class S3MediaDataStore(private val s3Client: S3Client, private val storageConfig: StorageConfig) : MediaDataStore {
    override suspend fun save(dataMediaSave: MediaSave): SavedMedia {
        val fileUploadRequest = PutObjectRequest.builder()
            .bucket(storageConfig.bucket)
            .key(dataMediaSave.name)
            .build()

        val thumbnailKey = "thumbnail-${dataMediaSave.name}"
        val thumbnailUploadRequest = PutObjectRequest.builder()
            .bucket(storageConfig.bucket)
            .key(thumbnailKey)
            .build()

        withContext(Dispatchers.IO) {
            awaitAll(
                async {
                    if (dataMediaSave.thumbnailInputStream != null) {
                        s3Client.putObject(
                            thumbnailUploadRequest,
                            RequestBody.fromBytes(dataMediaSave.thumbnailInputStream)
                        )
                        s3Client.utilities()
                            .getUrl(GetUrlRequest.builder().bucket(storageConfig.bucket).key(thumbnailKey).build())
                    } else {
                        null
                    }
                },
                async {
                    s3Client.putObject(fileUploadRequest, RequestBody.fromBytes(dataMediaSave.fileInputStream))
                    s3Client.utilities()
                        .getUrl(GetUrlRequest.builder().bucket(storageConfig.bucket).key(dataMediaSave.name).build())
                }
            )
        }
        return SuccessSavedMedia(
            name = dataMediaSave.name,
            url = "${storageConfig.publicUrl}/${storageConfig.bucket}/${dataMediaSave.name}",
            thumbnailUrl = "${storageConfig.publicUrl}/${storageConfig.bucket}/$thumbnailKey"
        )
    }

    override suspend fun delete(id: String) {
        val fileDeleteRequest = DeleteObjectRequest.builder().bucket(storageConfig.bucket).key(id).build()
        val thumbnailDeleteRequest =
            DeleteObjectRequest.builder().bucket(storageConfig.bucket).key("thumbnail-$id").build()
        s3Client.deleteObject(fileDeleteRequest)
        s3Client.deleteObject(thumbnailDeleteRequest)
    }
}
