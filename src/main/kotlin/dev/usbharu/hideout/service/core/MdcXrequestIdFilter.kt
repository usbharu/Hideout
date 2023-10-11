package dev.usbharu.hideout.service.core

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Service
import java.util.*

@Service
class MdcXrequestIdFilter : Filter {
    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain) {
        val uuid = UUID.randomUUID()
        try {
            MDC.put(KEY, uuid.toString())
            chain.doFilter(request, response)
        } finally {
            MDC.remove(KEY)
        }
    }

    companion object {
        private const val KEY = "x-request-id"
    }
}
