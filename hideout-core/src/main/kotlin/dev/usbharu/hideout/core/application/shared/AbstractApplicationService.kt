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

package dev.usbharu.hideout.core.application.shared

import kotlinx.coroutines.CancellationException
import org.slf4j.Logger

abstract class AbstractApplicationService<T : Any, R>(
    protected val transaction: Transaction,
    protected val logger: Logger,
) : ApplicationService<T, R> {
    override suspend fun execute(command: T, executor: CommandExecutor): R {
        return try {
            logger.debug("START ${command::class.simpleName} by $executor")
            val response = transaction.transaction<R> {
                internalExecute(command, executor)
            }
            logger.info("SUCCESS ${command::class.simpleName} by ${executor.executor}")
            response
        } catch (e: CancellationException) {
            logger.debug("Coroutine canceled", e)
            throw e
        } catch (e: Exception) {
            logger.warn("Command execution error", e)
            throw e
        }

    }

    protected abstract suspend fun internalExecute(command: T, executor: CommandExecutor): R
}