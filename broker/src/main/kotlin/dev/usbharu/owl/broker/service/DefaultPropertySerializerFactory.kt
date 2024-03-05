package dev.usbharu.owl.broker.service

import dev.usbharu.owl.common.property.*
import org.koin.core.annotation.Singleton

@Singleton(binds = [PropertySerializerFactory::class])
class DefaultPropertySerializerFactory :
    CustomPropertySerializerFactory(
        setOf(
            IntegerPropertySerializer(),
            StringPropertyValueSerializer(),
            DoublePropertySerializer(),
            BooleanPropertySerializer()
        )
    )