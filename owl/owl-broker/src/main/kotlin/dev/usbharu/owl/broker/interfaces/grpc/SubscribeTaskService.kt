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

import dev.usbharu.owl.Consumer
import dev.usbharu.owl.SubscribeTaskServiceGrpcKt.SubscribeTaskServiceCoroutineImplBase
import dev.usbharu.owl.broker.external.toUUID
import dev.usbharu.owl.broker.service.ConsumerService
import dev.usbharu.owl.broker.service.RegisterConsumerRequest
import org.koin.core.annotation.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Singleton
class SubscribeTaskService(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    private val consumerService: ConsumerService
) :
    SubscribeTaskServiceCoroutineImplBase(coroutineContext) {
    override suspend fun subscribeTask(request: Consumer.SubscribeTaskRequest): Consumer.SubscribeTaskResponse {
        val id =
            consumerService.registerConsumer(RegisterConsumerRequest(request.name, request.hostname, request.tasksList))
        return Consumer.SubscribeTaskResponse.newBuilder().setId(id.toUUID()).build()
    }
}