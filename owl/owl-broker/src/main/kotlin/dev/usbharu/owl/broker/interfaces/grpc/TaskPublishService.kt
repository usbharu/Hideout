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

package dev.usbharu.owl.broker.interfaces.grpc

import dev.usbharu.owl.PublishTaskOuterClass
import dev.usbharu.owl.PublishTaskOuterClass.PublishedTask
import dev.usbharu.owl.PublishTaskOuterClass.PublishedTasks
import dev.usbharu.owl.TaskPublishServiceGrpcKt.TaskPublishServiceCoroutineImplBase
import dev.usbharu.owl.broker.external.toUUID
import dev.usbharu.owl.broker.service.PublishTask
import dev.usbharu.owl.broker.service.TaskPublishService
import dev.usbharu.owl.common.property.PropertySerializeUtils
import dev.usbharu.owl.common.property.PropertySerializerFactory
import io.grpc.Status
import io.grpc.StatusException
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class TaskPublishService(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    private val taskPublishService: TaskPublishService,
    private val propertySerializerFactory: PropertySerializerFactory
) :
    TaskPublishServiceCoroutineImplBase(coroutineContext) {

    override suspend fun publishTask(request: PublishTaskOuterClass.PublishTask): PublishedTask {

        logger.warn("aaaaaaaaaaa")



        return try {

            val publishedTask = taskPublishService.publishTask(
                PublishTask(
                    request.name,
                    request.producerId.toUUID(),
                    PropertySerializeUtils.deserialize(propertySerializerFactory, request.propertiesMap)
                )
            )
            PublishedTask.newBuilder().setName(publishedTask.name).setId(publishedTask.id.toUUID()).build()
        } catch (e: Throwable) {
            logger.warn("exception ", e)
            throw StatusException(Status.INTERNAL)
        }
    }

    override suspend fun publishTasks(request: PublishTaskOuterClass.PublishTasks): PublishedTasks {

        val tasks = request.propertiesArrayList.map {
            PublishTask(
                request.name,
                request.producerId.toUUID(),
                PropertySerializeUtils.deserialize(propertySerializerFactory, it.propertiesMap)
            )
        }

        val publishTasks = taskPublishService.publishTasks(tasks)

        return PublishedTasks.newBuilder().setName(request.name).addAllId(publishTasks.map { it.id.toUUID() }).build()
    }

    companion object {
        private val logger =
            LoggerFactory.getLogger(dev.usbharu.owl.broker.interfaces.grpc.TaskPublishService::class.java)
    }
}