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

import dev.usbharu.hideout.application.config.S3StorageConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetUrlRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Service
@ConditionalOnProperty("hideout.storage.type", havingValue = "s3")
class S3MediaDataStore(private val s3Client: S3Client, private val s3StorageConfig: S3StorageConfig) : MediaDataStore {
    override suspend fun save(dataMediaSave: MediaSave): SavedMedia {
        val fileUploadRequest = PutObjectRequest.builder()
            .bucket(s3StorageConfig.bucket)
            .key(dataMediaSave.name)
            .build()

        val thumbnailKey = "thumbnail-${dataMediaSave.name}"
        val thumbnailUploadRequest = PutObjectRequest.builder()
            .bucket(s3StorageConfig.bucket)
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
                            .getUrl(GetUrlRequest.builder().bucket(s3StorageConfig.bucket).key(thumbnailKey).build())
                    } else {
                        null
                    }
                },
                async {
                    s3Client.putObject(fileUploadRequest, RequestBody.fromBytes(dataMediaSave.fileInputStream))
                    s3Client.utilities()
                        .getUrl(GetUrlRequest.builder().bucket(s3StorageConfig.bucket).key(dataMediaSave.name).build())
                }
            )
        }
        return SuccessSavedMedia(
            name = dataMediaSave.name,
            url = "${s3StorageConfig.publicUrl}/${s3StorageConfig.bucket}/${dataMediaSave.name}",
            thumbnailUrl = "${s3StorageConfig.publicUrl}/${s3StorageConfig.bucket}/$thumbnailKey"
        )
    }

    override suspend fun save(dataSaveRequest: MediaSaveRequest): SavedMedia {
        logger.info("MEDIA upload. {}", dataSaveRequest.name)

        val fileUploadRequest = PutObjectRequest.builder()
            .bucket(s3StorageConfig.bucket)
            .key(dataSaveRequest.name)
            .build()

        logger.info("MEDIA upload. bucket: {} key: {}", s3StorageConfig.bucket, dataSaveRequest.name)

        val thumbnailKey = "thumbnail-${dataSaveRequest.name}"
        val thumbnailUploadRequest = PutObjectRequest.builder()
            .bucket(s3StorageConfig.bucket)
            .key(thumbnailKey)
            .build()

        logger.info("MEDIA upload. bucket: {} key: {}", s3StorageConfig.bucket, thumbnailKey)

        withContext(Dispatchers.IO) {
            awaitAll(
                async {
                    if (dataSaveRequest.thumbnailPath != null) {
                        s3Client.putObject(
                            thumbnailUploadRequest,
                            RequestBody.fromFile(dataSaveRequest.thumbnailPath)
                        )
                    } else {
                        null
                    }
                },
                async {
                    s3Client.putObject(fileUploadRequest, RequestBody.fromFile(dataSaveRequest.filePath))
                }
            )
        }
        val successSavedMedia = SuccessSavedMedia(
            name = dataSaveRequest.name,
            url = "${s3StorageConfig.publicUrl}/${s3StorageConfig.bucket}/${dataSaveRequest.name}",
            thumbnailUrl = "${s3StorageConfig.publicUrl}/${s3StorageConfig.bucket}/$thumbnailKey"
        )

        logger.info("SUCCESS Media upload. {}", dataSaveRequest.name)
        logger.debug(
            "name: {} url: {} thumbnail url: {}",
            successSavedMedia.name,
            successSavedMedia.url,
            successSavedMedia.thumbnailUrl
        )

        return successSavedMedia
    }

    override suspend fun delete(id: String) {
        val fileDeleteRequest = DeleteObjectRequest.builder().bucket(s3StorageConfig.bucket).key(id).build()
        val thumbnailDeleteRequest =
            DeleteObjectRequest.builder().bucket(s3StorageConfig.bucket).key("thumbnail-$id").build()
        s3Client.deleteObject(fileDeleteRequest)
        s3Client.deleteObject(thumbnailDeleteRequest)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(S3MediaDataStore::class.java)
    }
}
