package no.nav.sokos.trekk.mq

import io.prometheus.metrics.core.metrics.Counter
import jakarta.jms.Queue

interface JmsProducer {
    fun send(
        payload: String,
        senderQueue: Queue,
        metricCounter: Counter,
    )
}
