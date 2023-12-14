package dev.usbharu.hideout

import com.fasterxml.jackson.module.kotlin.isKotlinClass
import com.jparams.verifier.tostring.ToStringVerifier
import com.jparams.verifier.tostring.preset.Presets
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import nl.jqno.equalsverifier.internal.reflection.PackageScanner
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RestController
import java.lang.reflect.Modifier

class EqualsAndToStringTest {
    @TestFactory
    fun equalsTest(): List<DynamicTest> {

        val classes = PackageScanner.getClassesIn("dev.usbharu.hideout", null, true)

        return classes
            .asSequence()
            .filter {
                it.getAnnotation(Service::class.java) == null
            }
            .filter {
                it.getAnnotation(Repository::class.java) == null
            }
            .filter {
                it.getAnnotation(Component::class.java) == null
            }
            .filter {
                it.getAnnotation(Controller::class.java) == null
            }
            .filter {
                it.getAnnotation(RestController::class.java) == null
            }
            .filter {
                it.getAnnotation(Configuration::class.java) == null
            }
            .filterNot {
                it.packageName.startsWith("dev.usbharu.hideout.domain.mastodon.model.generated")
            }
            .filterNot {
                Throwable::class.java.isAssignableFrom(it)
            }
            .filterNot {
                Modifier.isAbstract(it.modifiers)
            }
            .filter {
                try {
                    it.kotlin.objectInstance == null
                } catch (_: Exception) {
                    true
                }

            }
            .filter {
                it.superclass == Any::class.java || it.superclass?.packageName?.startsWith("dev.usbharu") ?: true
            }
            .map {
                dynamicTest(it.name) {
                    if (it.isKotlinClass()) {
                        println(" at ${it.name}.toString(${it.simpleName}.kt:1)")
                    }
                    try {
                        EqualsVerifier.simple()
                            .suppress(Warning.INHERITED_DIRECTLY_FROM_OBJECT, Warning.TRANSIENT_FIELDS)
                            .forClass(it)
                            .verify()
                    } catch (e: AssertionError) {
                        e.printStackTrace()
                    }
                }
            }
            .toList()
    }

    @TestFactory
    fun toStringTest(): List<DynamicTest> {

        return PackageScanner.getClassesIn("dev.usbharu.hideout", null, true)
            .filter {
                it != null && !it.isEnum && !it.isInterface && !Modifier.isAbstract(it.modifiers)
            }
            .filter {
                val clazz = it.getMethod(it::toString.name).declaringClass
                clazz != Any::class.java && clazz != Throwable::class.java
            }
            .filter {
                it.superclass == Any::class.java || it.superclass?.packageName?.startsWith("dev.usbharu") ?: true
            }
            .map {

                dynamicTest(it.name) {
                    if (it.isKotlinClass()) {
                        println(" at ${it.name}.toString(${it.simpleName}.kt:1)")
                    }
                    try {
                        ToStringVerifier.forClass(it).withPreset(Presets.INTELLI_J).verify()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }
}
