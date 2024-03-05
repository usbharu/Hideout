package dev.usbharu.owl.broker.service

import dev.usbharu.owl.common.property.CustomPropertySerializerFactory
import dev.usbharu.owl.common.property.IntegerPropertySerializer
import dev.usbharu.owl.common.property.PropertySerializerFactory
import org.koin.core.annotation.Singleton

@Singleton(binds = [PropertySerializerFactory::class])
class DefaultPropertySerializerFactory : CustomPropertySerializerFactory(setOf(IntegerPropertySerializer()))