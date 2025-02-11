package no.nav.sokos.trekk.mq

import java.time.LocalDate

import com.ibm.mq.jakarta.jms.MQQueue
import jakarta.jms.ConnectionFactory
import jakarta.jms.JMSContext
import jakarta.jms.Message
import jakarta.jms.Queue
import mu.KotlinLogging

import no.nav.sokos.trekk.config.MQConfig
import no.nav.sokos.trekk.config.PropertiesConfig
import no.nav.sokos.trekk.metrics.Metrics.mqTrekkInnBoqMetricCounter
import no.nav.sokos.trekk.service.BehandleTrekkvedtakService

private val logger = KotlinLogging.logger {}

class JmsListenerService(
    connectionFactory: ConnectionFactory = MQConfig.connectionFactory(),
    trekkInnQueue: Queue = MQQueue(PropertiesConfig.MQProperties().trekkInnQueueName),
    private val trekkInnBoqQueue: Queue = MQQueue(PropertiesConfig.MQProperties().trekkInnBoqQueueName),
    private val producer: JmsProducerService = JmsProducerService(),
    private val behandleTrekkvedtakService: BehandleTrekkvedtakService = BehandleTrekkvedtakService(),
) {
    private val jmsContext: JMSContext = connectionFactory.createContext(JMSContext.CLIENT_ACKNOWLEDGE)
    private val trekkInnListener = jmsContext.createConsumer(trekkInnQueue)

    init {
        trekkInnListener.setMessageListener { onTrekkInnMessage(it) }
        jmsContext.setExceptionListener { logger.error("Feil på MQ-kommunikasjon", it) }
    }

    fun start() {
        jmsContext.start()
    }

    private fun onTrekkInnMessage(message: Message) {
        val jmsMessage = message.getBody(String::class.java)
        runCatching {
            logger.debug { "Mottatt Trekk fra OppdragZ. Meldingsinnhold: $jmsMessage" }
            behandleTrekkvedtakService.behandleTrekkvedtak(jmsMessage, LocalDate.now())
            message.acknowledge()
        }.onFailure { exception ->
            logger.error(exception) { "Prosessering av utbetalingsmeldingretur feilet. ${message.jmsMessageID}" }
            producer.send(jmsMessage, trekkInnBoqQueue, mqTrekkInnBoqMetricCounter)
        }
    }
}
