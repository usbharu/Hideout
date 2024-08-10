package dev.usbharu.hideout.core.domain.model.support.acct

data class Acct(
    val userpart: String,
    val host: String
) {
    override fun toString(): String {
        return "acct:$userpart@$host"
    }
}
