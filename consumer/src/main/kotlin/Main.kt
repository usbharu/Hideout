package dev.usbharu

import dev.usbharu.owl.*
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

suspend fun main() {

    val channel = ManagedChannelBuilder.forAddress(
        "localhost", 50051
    ).build()
    val subscribeTaskServiceCoroutineStub = SubscribeTaskServiceGrpcKt.SubscribeTaskServiceCoroutineStub(channel)

    val assignmentTaskServiceCoroutineStub = AssignmentTaskServiceGrpcKt.AssignmentTaskServiceCoroutineStub(
        channel
    )

    val taskResultServiceCoroutineStub = TaskResultServiceGrpcKt.TaskResultServiceCoroutineStub(channel)

    val subscribeTask = subscribeTaskServiceCoroutineStub.subscribeTask(subscribeTaskRequest {
        this.name = ""
        this.hostname = ""
        this.tasks.addAll(listOf())
    })

    val concurrent = MutableStateFlow(64)
    val processing = MutableStateFlow(0)


    coroutineScope {
        launch(Dispatchers.Default) {
            taskResultServiceCoroutineStub.tasKResult(flow {
                assignmentTaskServiceCoroutineStub
                    .ready(
                        flow {
                            while (this@coroutineScope.isActive) {

                                val andSet = concurrent.getAndUpdate {
                                    0
                                }


                                if (andSet != 0) {
                                    emit(readyRequest {
                                        this.consumerId = subscribeTask.id
                                        this.numberOfConcurrent = andSet
                                    })
                                    continue
                                }
                                delay(100)

                                concurrent.update {
                                    ((64 - it) - processing.value).coerceIn(0, 64 - max(0, processing.value))
                                }
                            }
                        }
                    )
                    .onEach {

                        processing.update { it + 1 }

                        try {
                            emit(taskResult {

                            })

                        } catch (e: Exception) {
                            emit(taskResult {
                                this.success = false
                            })
                        } finally {
                            processing.update { it - 1 }
                            concurrent.update {
                                if (it < 64) {
                                    it + 1
                                } else {
                                    64
                                }
                            }
                        }
                    }
                    .flowOn(Dispatchers.Default)
                    .collect()
            })
        }
    }
}