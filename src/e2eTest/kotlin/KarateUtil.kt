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
}
