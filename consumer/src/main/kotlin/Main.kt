package dev.usbharu

import dev.usbharu.owl.AssignmentTaskServiceGrpcKt
import dev.usbharu.owl.readyRequest
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

suspend fun main() {
    withContext(Dispatchers.Default) {
        var isReady = true
        AssignmentTaskServiceGrpcKt.AssignmentTaskServiceCoroutineStub(
            ManagedChannelBuilder.forAddress(
                "localhost", 50051
            ).build()
        ).ready(flow {
            while (isActive) {
                if (isReady) {
                    emit(readyRequest {
                        this.consumerId
                    })
                }
                delay(500)
            }
        }).onEach {

        }.collect()


    }
}