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

package dev.usbharu.owl.producer.api

import dev.usbharu.owl.common.task.PublishedTask
import dev.usbharu.owl.common.task.Task
import dev.usbharu.owl.common.task.TaskDefinition

/**
 * タスクを発生させるクライアント
 *
 */
interface OwlProducer {

    /**
     * Producerを開始します
     *
     */
    suspend fun start()

    /**
     * タスク定義を登録します
     *
     * @param T 登録するタスク
     * @param taskDefinition 登録するタスクの定義
     */
    suspend fun <T : Task> registerTask(taskDefinition: TaskDefinition<T>)

    /**
     * タスクを公開します。タスクは定義済みである必要があります。
     *
     * @param T 公開するタスク
     * @param task タスクの詳細
     * @return 公開されたタスク
     */
    suspend fun <T : Task> publishTask(task: T): PublishedTask<T>
}
