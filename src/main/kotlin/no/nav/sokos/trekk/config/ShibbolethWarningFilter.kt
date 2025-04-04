package no.nav.sokos.trekk.config

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.boolex.EvaluationException
import ch.qos.logback.core.boolex.EventEvaluatorBase

/**
 * Use this to filter unwanted log messages.
 */
class ShibbolethWarningFilter : EventEvaluatorBase<ILoggingEvent>() {
    @Throws(NullPointerException::class, EvaluationException::class)
    override fun evaluate(event: ILoggingEvent): Boolean {
        val message = event.message
        return message.contains("net.shibboleth.utilities.java.support.primitive.StringSupport")
    }
}
