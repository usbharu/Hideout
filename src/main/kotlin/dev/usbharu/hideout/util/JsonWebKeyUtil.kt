package dev.usbharu.hideout.util

import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*

object JsonWebKeyUtil {

    fun publicKeyToJwk(publicKey: String): String {
        val x509EncodedKeySpec = X509EncodedKeySpec(Base64.getDecoder().decode(publicKey))
        val generatePublic = KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec)
        return publicKeyToJwk(generatePublic as RSAPublicKey)
    }

    fun publicKeyToJwk(publicKey: RSAPublicKey): String {
        val e = encodeBase64UInt(publicKey.publicExponent)
        val n = encodeBase64UInt(publicKey.modulus)
        return """{"keys":[{"e":"$e","n":"$n","use":"sig","kty":"RSA"}]}"""
    }

    private fun encodeBase64UInt(bigInteger: BigInteger, minLength: Int = -1): String {
        if(bigInteger.signum() < 0){
            throw IllegalArgumentException("Cannot encode negative numbers")
        }

        var bytes = bigInteger.toByteArray()
        if (bigInteger.bitLength() % 8 == 0 && (bytes[0] == 0.toByte()) && bytes.size > 1){
             bytes = Arrays.copyOfRange(bytes, 1, bytes.size)
        }
        if (minLength != -1){
            if (bytes.size < minLength){
                val array = ByteArray(minLength)
                System.arraycopy(bytes, 0, array, minLength - bytes.size, bytes.size)
                bytes = array
            }
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
