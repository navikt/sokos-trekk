package no.nav.sokos.trekk.mq

import com.ibm.msg.client.jakarta.jms.JmsConstants.SESSION_TRANSACTED
import io.prometheus.metrics.core.metrics.Counter
import jakarta.jms.ConnectionFactory
import jakarta.jms.JMSContext
import jakarta.jms.Queue
import mu.KotlinLogging

import no.nav.sokos.trekk.config.MQConfig
import no.nav.sokos.trekk.exception.TrekkException

private val logger = KotlinLogging.logger {}

class JmsProducerService(
    connectionFactory: ConnectionFactory = MQConfig.connectionFactory(),
) : JmsProducer {
    private val jmsContext: JMSContext = connectionFactory.createContext()

    override fun send(
        payload: String,
        senderQueue: Queue,
        metricCounter: Counter,
    ) {
        jmsContext.createContext(SESSION_TRANSACTED).use { context ->
            runCatching {
                context.createProducer().send(senderQueue, payload)
            }.onSuccess {
                context.commit()
                metricCounter.inc()
                logger.debug { "MQ-transaksjon committed meldingen: $payload" }
            }.onFailure { exception ->
                context.rollback()
                logger.error(exception) { "MQ-transaksjon rolled back" }
                throw TrekkException(exception.message ?: "Feil ved sending av melding til MQ", exception)
            }
        }
    }
}
