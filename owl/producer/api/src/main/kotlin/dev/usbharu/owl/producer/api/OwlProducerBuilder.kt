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

/**
 * [OwlProducer]を作成するビルダー
 *
 * @param P 作成する[OwlProducer]
 * @param T [OwlProducer]の構成
 */
interface OwlProducerBuilder<P : OwlProducer, T : OwlProducerConfig> {
    /**
     * 現在の構成を返します
     *
     * @return 現在の構成
     */
    fun config(): T

    /**
     * 構成を適用します
     *
     * @param owlProducerConfig 適用する構成
     */
    fun apply(owlProducerConfig: T)

    /**
     * 適用されている構成を使用して[OwlProducer]のインスタンスを作成します。
     *
     * @return 作成された[OwlProducer]
     */
    fun build(): P
}