package no.nav.sokos.trekk.mq

import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

import com.ibm.mq.jakarta.jms.MQQueue
import com.ibm.msg.client.jakarta.wmq.WMQConstants
import jakarta.jms.ConnectionFactory
import jakarta.jms.JMSContext
import jakarta.jms.Message
import jakarta.jms.Queue
import mu.KotlinLogging

import no.nav.sokos.trekk.config.MQConfig
import no.nav.sokos.trekk.config.PropertiesConfig
import no.nav.sokos.trekk.metrics.Metrics.mqTrekkInnBoqMetricCounter
import no.nav.sokos.trekk.metrics.Metrics.mqTrekkInnMetricCounter
import no.nav.sokos.trekk.service.BehandleTrekkvedtakService
import no.nav.sokos.trekk.util.TraceUtils

private val logger = KotlinLogging.logger {}

class JmsListenerService(
    connectionFactory: ConnectionFactory = MQConfig.connectionFactory(),
    trekkInnQueue: Queue = MQQueue(PropertiesConfig.MQProperties().trekkInnQueueName).apply { targetClient = WMQConstants.WMQ_CLIENT_NONJMS_MQ },
    private val trekkInnBoqQueue: Queue = MQQueue(PropertiesConfig.MQProperties().trekkInnBoqQueueName).apply { targetClient = WMQConstants.WMQ_CLIENT_NONJMS_MQ },
    private val producer: JmsProducer = JmsProducerService(),
    private val behandleTrekkvedtakService: BehandleTrekkvedtakService = BehandleTrekkvedtakService(),
) : AutoCloseable {
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
        TraceUtils.withTracerId {
            runCatching {
                logger.debug { "Mottatt Trekk fra OppdragZ. Meldingsinnhold: $jmsMessage" }
                behandleTrekkvedtakService.behandleTrekkvedtak(
                    xmlContent = jmsMessage,
                    fromDate = LocalDate.now(),
                    toDate = LocalDate.now().plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()),
                )
                message.acknowledge()
                mqTrekkInnMetricCounter.inc()
            }.onFailure { exception ->
                logger.error(exception) { "Prosessering av utbetalingsmeldingretur feilet. ${message.jmsMessageID}" }
                producer.send(jmsMessage, trekkInnBoqQueue, mqTrekkInnBoqMetricCounter)
            }
        }
    }

    override fun close() {
        runCatching { trekkInnListener.close() }
            .onFailure { logger.warn(it) { "Failed to close JMS consumer" } }
        runCatching { jmsContext.close() }
            .onFailure { logger.warn(it) { "Failed to close JMS context" } }
    }
}
