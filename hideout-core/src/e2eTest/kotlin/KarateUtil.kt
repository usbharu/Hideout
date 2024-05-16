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

import com.intuit.karate.junit5.Karate

object KarateUtil {
    fun springBootKarateTest(path: String, scenario: String, clazz: Class<*>, port: String): Karate {
        if (scenario.isEmpty()) {
            return Karate.run(path).relativeTo(clazz).systemProperty("karate.port", port).karateEnv("dev")
        } else {
            return Karate.run(path).scenarioName(scenario).relativeTo(clazz).systemProperty("karate.port", port)
                .karateEnv("dev")
        }
    }

    fun e2eTest(path: String, scenario: String = "", properties: Map<String, String>, clazz: Class<*>): Karate {
        val run = Karate.run(path)

        val karate = if (scenario.isEmpty()) {
            run
        } else {
            run.scenarioName(scenario)
        }

        var relativeTo = karate.relativeTo(clazz)

        properties.map { relativeTo = relativeTo.systemProperty(it.key, it.value) }

        return relativeTo.karateEnv("dev")
    }
}
