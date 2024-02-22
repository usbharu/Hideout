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

package dev.usbharu.testmvc

import dev.usbharu.testmvc.path.Path
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request

class TestMvc(mockMvc: MockMvc, private val configuration: Configuration) {
    private val mockMvc = mockMvc


    fun request(path: Path, allowMethods: Set<HttpMethod>): List<DynamicNode> {

        val otherMethods = HttpMethod.values().toMutableList().minus(allowMethods)

        return path.buildUrls().flatMap { endpoint ->

            val dynamicTests = allowMethods.map {
                dynamicContainer("${it.name()} $endpoint", mutableListOf())
            }

            val otherTests = otherMethods.map {
                dynamicTest("${it.name()} $endpoint") {
                    val request = mockMvc
                        .request(it, endpoint) {

                        }
                        .let {
                            if (it.andReturn().request.isAsyncSupported) {
                                it.asyncDispatch()
                            } else {
                                it
                            }
                        }
                        .andDo { print() }
                        .andExpect { status { isMethodNotAllowed() } }

                    request.andReturn().response.contentAsByteArray

                }
            }
            dynamicTests.plus(dynamicContainer("OTHER $endpoint", otherTests.toMutableList()))
        }
    }

    fun request(testDefinition: TestDefinition): List<DynamicTest> {
        val urls = testDefinition.path.buildUrls()

        return urls.map { url ->
            dynamicTest("${testDefinition.method.name()} $url") {
                val mvcResult = mockMvc
                    .request(testDefinition.method, url) {
                        this.apply(testDefinition.requestDsl)
                    }
                    .let { resultActionsDsl ->
                        if (resultActionsDsl.andReturn().request.isAsyncSupported) {
                            resultActionsDsl.asyncDispatch()
                        } else {
                            resultActionsDsl
                        }
                    }
                    .andDo {
                        print()
                    }
                    .andExpect {
                        status {
                            isEqualTo(testDefinition.expectStatus.value())
                        }
                    }

                    .andReturn()



                configuration.objectMapper.readValue(
                    mvcResult.response.contentAsString,
                    testDefinition.deserializeClass.java
                )
            }
        }
    }
}