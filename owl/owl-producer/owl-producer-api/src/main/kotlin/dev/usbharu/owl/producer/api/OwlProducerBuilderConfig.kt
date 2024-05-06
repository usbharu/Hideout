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
 * [OwlProducerBuilder]と[OwlProducerConfig]を使用して[OwlProducer]のインスタンスを作成します。
 *
 * @param P 作成する[OwlProducer]
 * @param T 作成に使用する[OwlProducerBuilder]
 * @param C 構成
 * @param owlProducerBuilder 作成に使用する[OwlProducerBuilder]
 * @param configBlock 構成
 */
fun <P : OwlProducer, T : OwlProducerBuilder<P, C>, C : OwlProducerConfig> OWL(
    owlProducerBuilder: T,
    configBlock: C.() -> Unit = {},
): P {
    owlProducerBuilder.apply(owlProducerBuilder.config().apply { configBlock() })
    return owlProducerBuilder.build()
}