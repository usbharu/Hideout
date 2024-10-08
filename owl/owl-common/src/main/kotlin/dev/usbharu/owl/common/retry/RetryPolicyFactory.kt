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

package dev.usbharu.owl.common.retry

import org.slf4j.LoggerFactory

interface RetryPolicyFactory {
    fun factory(name: String): RetryPolicy
}

class DefaultRetryPolicyFactory(private val map: Map<String, RetryPolicy>) : RetryPolicyFactory {
    override fun factory(name: String): RetryPolicy = map[name] ?: throwException(name)

    private fun throwException(name: String): Nothing {
        logger.warn("RetryPolicy not found. name: {}", name)
        throw RetryPolicyNotFoundException("RetryPolicy not found. name: $name")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RetryPolicyFactory::class.java)
    }
}
