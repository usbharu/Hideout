package dev.usbharu.hideout.core.domain.model.support.acct

data class Acct(
    val userpart: String,
    val host: String
) {
    override fun toString(): String = "acct:$userpart@$host"

    companion object {

        fun of(acct: String): Acct {
            return Acct(
                acct.substringBeforeLast('@'),
                acct.substringAfterLast('@', "")
            )
        }
    }
}
