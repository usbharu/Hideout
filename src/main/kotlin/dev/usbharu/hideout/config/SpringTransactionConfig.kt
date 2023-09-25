package dev.usbharu.hideout.config

import org.jetbrains.exposed.spring.SpringTransactionManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.annotation.TransactionManagementConfigurer
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
class SpringTransactionConfig(val datasource: DataSource) : TransactionManagementConfigurer {
    @Bean
    override fun annotationDrivenTransactionManager(): PlatformTransactionManager =
        SpringTransactionManager(datasource)
}
