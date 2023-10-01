package dev.usbharu.hideout.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration

@Configuration
class JdbcConfiguration : AbstractJdbcConfiguration() {
    override fun jdbcCustomConversions(): JdbcCustomConversions {
        return JdbcCustomConversions(
            listOf(
                InstantLongConverter.INSTANCE, InstantJavaLongConverter.INSTANCE
            )
        )
    }
}
