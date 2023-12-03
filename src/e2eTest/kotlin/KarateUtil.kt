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
