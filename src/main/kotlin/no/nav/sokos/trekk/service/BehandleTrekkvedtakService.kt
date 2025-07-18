package no.nav.sokos.trekk.service

import java.math.RoundingMode
import java.time.LocalDate

import com.ibm.mq.jakarta.jms.MQQueue
import com.ibm.msg.client.jakarta.wmq.WMQConstants
import jakarta.jms.Queue
import mu.KotlinLogging

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak
import no.nav.maskinelletrekk.trekk.v1.ObjectFactory
import no.nav.maskinelletrekk.trekk.v1.Trekk
import no.nav.maskinelletrekk.trekk.v1.TypeKjoring
import no.nav.sokos.trekk.arenamock.ArenaMockService
import no.nav.sokos.trekk.config.PropertiesConfig
import no.nav.sokos.trekk.metrics.Metrics
import no.nav.sokos.trekk.metrics.TAG_EXCEPTION_NAME
import no.nav.sokos.trekk.mq.JmsProducerService
import no.nav.sokos.trekk.soap.ArenaClientService
import no.nav.sokos.trekk.util.JaxbUtils
import no.nav.sokos.trekk.util.JaxbUtils.unmarshalTrekk
import no.nav.sokos.trekk.util.Utils.toXMLGregorianCalendar
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Periode
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Person
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Tema
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeRequest
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeResponse

private val logger = KotlinLogging.logger { }
val TEMA_CODE = listOf("AAP", "DAG", "IND")

class BehandleTrekkvedtakService(
    private val arenaClientService: ArenaClientService = ArenaClientService(),
    private val producer: JmsProducerService = JmsProducerService(),
    private val replyQueue: Queue = MQQueue(PropertiesConfig.MQProperties().trekkReplyQueueName).apply { targetClient = WMQConstants.WMQ_CLIENT_NONJMS_MQ },
    private val replyBatchQueue: Queue = MQQueue(PropertiesConfig.MQProperties().trekkReplyBatchQueueName).apply { targetClient = WMQConstants.WMQ_CLIENT_NONJMS_MQ },
) {
    fun behandleTrekkvedtak(
        xmlContent: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        replyToQueue: Boolean = true,
    ): Trekk =
        runCatching {
            val trekk = unmarshalTrekk(xmlContent)

            val typeKjoring = trekk.typeKjoring
            val trekkRequestList = trekk.trekkRequest.distinctBy { it.trekkvedtakId }

            logger.info { "Starter behandling av ${trekkRequestList.size} trekkvedtak." }

            val fnrSet = trekkRequestList.map { it.offnr }.toSet()
            val ytelseVedtakMap =
                when {
                    PropertiesConfig.Configuration().useArenaMock -> ArenaMockService.hentYtelsesVedtak(fnrSet, fromDate, toDate)
                    else -> hentYtelsesVedtak(fnrSet, fromDate, toDate)
                }

            val trekkResponseList = trekkRequestList.map { VedtaksBeregningService(ytelseVedtakMap).invoke(it) }

            logger.info { "Behandlet trekkvedtak: ${trekkResponseList.size} med type kjøring: $typeKjoring" }

            val response =
                ObjectFactory().createTrekk().apply {
                    this.typeKjoring = typeKjoring
                    this.trekkResponse.addAll(trekkResponseList)
                }

            if (replyToQueue) {
                val replyXML = JaxbUtils.marshalTrekk(response)
                when (typeKjoring) {
                    TypeKjoring.PERI,
                    TypeKjoring.REME,
                    -> producer.send(replyXML, replyBatchQueue, Metrics.mqBatchReplyMetricCounter)

                    else -> producer.send(replyXML, replyQueue, Metrics.mqReplyMetricCounter)
                }
                logger.info { "Send trekkvedtak: ${response.trekkResponse.map { it.trekkvedtakId }} til OppdragZ." }
            }
            Metrics.behandletTrekkMetricCounter.inc(response.trekkResponse.size.toLong())
            response
        }.onFailure { exception ->
            logger.error(exception) { "Behandling av trekkvedtak feilet" }
            throw exception
        }.getOrThrow()

    private fun hentYtelsesVedtak(
        fnrSet: Set<String>,
        fromDate: LocalDate,
        toDate: LocalDate,
    ): Map<String, List<ArenaVedtak>> =
        runCatching {
            val request =
                FinnYtelseVedtakListeRequest().apply {
                    personListe.addAll(
                        fnrSet.map { fnr ->
                            Person().apply {
                                ident = fnr
                                periode =
                                    Periode().apply {
                                        fom = fromDate.toXMLGregorianCalendar()
                                        tom = toDate.toXMLGregorianCalendar()
                                    }
                            }
                        },
                    )
                    temaListe.addAll(TEMA_CODE.map { Tema().apply { value = it } }.toCollection(ArrayList()))
                }

            Metrics.soapArenaRequestCounter.inc()
            val response = arenaClientService.finnYtelseVedtakListe(request)
            Metrics.soapArenaResponseCounter.inc()

            response.mapToAreanVedtak()
        }.onFailure { exception ->
            Metrics.soapArenaErrorCounter.labelValues(TAG_EXCEPTION_NAME).inc()
            throw exception
        }.getOrThrow()
}

fun FinnYtelseVedtakListeResponse.mapToAreanVedtak(): Map<String, List<ArenaVedtak>> =
    this.personYtelseListe
        .flatMap { personYtelse ->
            personYtelse.sakListe.flatMap { sak ->
                sak.vedtakListe.map { vedtak ->
                    personYtelse.ident to
                        ArenaVedtak().apply {
                            dagsats = vedtak.dagsats.toBigDecimal().setScale(SUM_SCALE, RoundingMode.HALF_UP)
                            rettighetType = vedtak.rettighetstype.value
                            tema = sak.tema.value
                            vedtaksperiode =
                                no.nav.maskinelletrekk.trekk.v1.Periode().apply {
                                    fom = vedtak.vedtaksperiode.fom
                                    tom = vedtak.vedtaksperiode.tom
                                }
                        }
                }
            }
        }.groupBy({ it.first }, { it.second })
