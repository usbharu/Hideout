package dev.usbharu.hideout.util

import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

object RsaUtil {
    fun decodeRsaPublicKey(byteArray: ByteArray):RSAPublicKey{
        val x509EncodedKeySpec = X509EncodedKeySpec(byteArray)
        return KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec) as RSAPublicKey
    }

    fun decodeRsaPublicKey(encoded: String): RSAPublicKey = decodeRsaPublicKey(Base64Util.decode(encoded))

    fun decodeRsaPrivateKey(byteArray: ByteArray):RSAPrivateKey{
        val pkcS8EncodedKeySpec = PKCS8EncodedKeySpec(byteArray)
        return KeyFactory.getInstance("RSA").generatePrivate(pkcS8EncodedKeySpec) as RSAPrivateKey
    }

    fun decodeRsaPrivateKey(encoded: String):RSAPrivateKey  = decodeRsaPrivateKey(Base64Util.decode(encoded))
}
