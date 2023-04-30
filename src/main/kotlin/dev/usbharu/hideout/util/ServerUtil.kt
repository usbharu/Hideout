package dev.usbharu.hideout.util

object ServerUtil {
    fun getImplementationVersion():String = ServerUtil.javaClass.`package`.implementationVersion ?: "DEVELOPMENT-VERSION"
}
