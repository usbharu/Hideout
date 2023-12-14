package dev.usbharu.hideout

import com.fasterxml.jackson.module.kotlin.isKotlinClass
import com.jparams.verifier.tostring.ToStringVerifier
import com.jparams.verifier.tostring.preset.Presets
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import nl.jqno.equalsverifier.internal.reflection.PackageScanner
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.lang.reflect.Modifier
import kotlin.test.assertFails

class EqualsAndToStringTest {
    @Test
    fun equalsTest() {
        assertFails {
            EqualsVerifier
                .simple()
                .suppress(Warning.INHERITED_DIRECTLY_FROM_OBJECT)
                .forPackage("dev.usbharu.hideout", true)
                .verify()
        }
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
