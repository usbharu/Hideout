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

package dev.usbharu.testmvc.path

class StringParameter(value: String) : Parameter {
    private val internalList = listOf { value }
    override val size: Int
        get() = internalList.size

    override fun contains(element: () -> String): Boolean {
        return internalList.contains(element)
    }

    override fun containsAll(elements: Collection<() -> String>): Boolean {
        return internalList.containsAll(elements)
    }

    override fun get(index: Int): () -> String {
        return internalList.get(index)
    }

    override fun indexOf(element: () -> String): Int {
        return internalList.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        return internalList.isEmpty()
    }

    override fun iterator(): Iterator<() -> String> {
        return internalList.iterator()
    }

    override fun lastIndexOf(element: () -> String): Int {
        return internalList.lastIndexOf(element)
    }

    override fun listIterator(): ListIterator<() -> String> {
        return internalList.listIterator()
    }

    override fun listIterator(index: Int): ListIterator<() -> String> {
        return internalList.listIterator(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<() -> String> {
        return internalList.subList(fromIndex, toIndex)
    }
}