package dev.usbharu

import dev.usbharu.owl.*
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    var concurrent = 64
    val concurrentMutex = Mutex()
    var processing = 0
    val processingMutex = Mutex()

    coroutineScope {
        launch(Dispatchers.Default) {
            taskResultServiceCoroutineStub.tasKResult(flow {
                assignmentTaskServiceCoroutineStub
                    .ready(
                        flow {
                            while (isActive) {
                                val andSet = concurrentMutex.withLock {
                                    val andSet = concurrent
                                    concurrent = 0
                                    andSet
                                }
                                if (andSet != 0) {
                                    emit(readyRequest {
                                        this.consumerId = subscribeTask.id
                                        this.numberOfConcurrent = andSet
                                    })
                                    continue
                                }
                                delay(100)

                                val withLock = processingMutex.withLock {
                                    processing
                                }
                                concurrentMutex.withLock {
                                    concurrent = ((64 - concurrent) - withLock).coerceIn(0, 64 - max(0, withLock))
                                }
                            }
                        }
                    )
                    .onEach {
                        processingMutex.withLock {
                            processing++
                        }
                        try {
                            emit(taskResult {

                            })

                        } catch (e: Exception) {
                            emit(taskResult {
                                this.success = false
                            })
                        } finally {
                            processingMutex.withLock {
                                processing--
                            }
                            concurrentMutex.withLock {
                                if (concurrent < 64) {
                                    concurrent++
                                } else {
                                    concurrent = 64
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