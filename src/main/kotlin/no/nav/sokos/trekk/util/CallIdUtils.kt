package no.nav.sokos.trekk.util

import java.util.UUID

import io.ktor.http.HttpHeaders
import org.slf4j.MDC

object CallIdUtils {
    fun withCallId(block: () -> Unit) {
        val callId = MDC.get(HttpHeaders.XCorrelationId) ?: generateCallId()
        MDC.put(HttpHeaders.XCorrelationId, callId)
        try {
            block()
        } finally {
            MDC.remove(HttpHeaders.XCorrelationId)
        }
    }

    private fun generateCallId(): String {
        return UUID.randomUUID().toString()
    }
}
