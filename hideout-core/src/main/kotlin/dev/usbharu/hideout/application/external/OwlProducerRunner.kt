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

package dev.usbharu.hideout.application.external

import dev.usbharu.owl.common.task.TaskDefinition
import dev.usbharu.owl.producer.api.OwlProducer
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class OwlProducerRunner(private val owlProducer: OwlProducer, private val taskDefinitions: List<TaskDefinition<*>>) :
    ApplicationRunner, DisposableBean {
    override fun run(args: ApplicationArguments?) {
        runBlocking {
            owlProducer.start()
            taskDefinitions.forEach { taskDefinition -> owlProducer.registerTask(taskDefinition) }
        }
    }

    override fun destroy() {
        System.err.println("destroy aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
        runBlocking {
            owlProducer.stop()
        }
    }
}
