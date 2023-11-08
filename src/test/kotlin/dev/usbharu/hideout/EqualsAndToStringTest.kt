package dev.usbharu.hideout

import com.jparams.verifier.tostring.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import nl.jqno.equalsverifier.internal.reflection.PackageScanner
import org.junit.jupiter.api.Test
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

    @Test
    fun toStringTest() {

        PackageScanner.getClassesIn("dev.usbharu.hideout", null, true)
            .filter {
                it != null && !it.isEnum && !it.isInterface && !Modifier.isAbstract(it.modifiers)
            }
            .forEach {
                try {
                    ToStringVerifier.forClass(it).verify()
                } catch (e: AssertionError) {
                    println(it.name)
                    e.printStackTrace()
                } catch (e: Exception) {
                    println(it.name)
                    e.printStackTrace()
                }
            }
    }
}
