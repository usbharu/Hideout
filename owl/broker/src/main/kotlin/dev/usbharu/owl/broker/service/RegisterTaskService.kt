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

package dev.usbharu.owl.broker.service

import dev.usbharu.owl.broker.domain.exception.service.IncompatibleTaskException
import dev.usbharu.owl.broker.domain.model.taskdefinition.TaskDefinition
import dev.usbharu.owl.broker.domain.model.taskdefinition.TaskDefinitionRepository
import org.koin.core.annotation.Singleton
import org.slf4j.LoggerFactory

interface RegisterTaskService {
    suspend fun registerTask(taskDefinition: TaskDefinition)

    suspend fun unregisterTask(name:String)
}

@Singleton
class RegisterTaskServiceImpl(private val taskDefinitionRepository: TaskDefinitionRepository) : RegisterTaskService {
    override suspend fun registerTask(taskDefinition: TaskDefinition) {
        val definedTask = taskDefinitionRepository.findByName(taskDefinition.name)
        if (definedTask != null) {
            logger.debug("Task already defined. name: ${taskDefinition.name}")
            if (taskDefinition.propertyDefinitionHash != definedTask.propertyDefinitionHash) {
                throw IncompatibleTaskException("Task ${taskDefinition.name} has already been defined, and the parameters are incompatible.")
            }
            return
        }
        taskDefinitionRepository.save(taskDefinition)

        logger.info("Register a new task. name: {}",taskDefinition.name)
    }

    // todo すでにpublish済みのタスクをどうするか決めさせる
    override suspend fun unregisterTask(name: String) {
        taskDefinitionRepository.deleteByName(name)

        logger.info("Unregister a task. name: {}",name)
    }

    companion object{
        private val logger = LoggerFactory.getLogger(RegisterTaskServiceImpl::class.java)
    }
}