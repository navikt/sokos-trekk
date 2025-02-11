package no.nav.sokos.trekk.service

import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

import com.ibm.mq.jakarta.jms.MQQueue
import jakarta.jms.Queue
import mu.KotlinLogging

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak
import no.nav.maskinelletrekk.trekk.v1.ObjectFactory
import no.nav.maskinelletrekk.trekk.v1.Trekk
import no.nav.maskinelletrekk.trekk.v1.TypeKjoring
import no.nav.sokos.trekk.metrics.Metrics
import no.nav.sokos.trekk.metrics.Metrics.soapArenaErrorCounter
import no.nav.sokos.trekk.metrics.Metrics.soapArenaRequestCounter
import no.nav.sokos.trekk.metrics.Metrics.soapArenaResponseCounter
import no.nav.sokos.trekk.mq.JmsProducerService
import no.nav.sokos.trekk.soap.ArenaClientService
import no.nav.sokos.trekk.util.JaxbUtils
import no.nav.sokos.trekk.util.JaxbUtils.unmarshalTrekk
import no.nav.sokos.trekk.util.Utils.toXMLGregorianCalendar
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Periode
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Person
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Sak
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeRequest

private val logger = KotlinLogging.logger { }

class BehandleTrekkvedtakService(
    private val arenaClientService: ArenaClientService = ArenaClientService(),
    private val producer: JmsProducerService = JmsProducerService(),
    private val replyQueue: Queue = MQQueue(),
    private val batchReplyQueue: Queue = MQQueue(),
) {
    fun behandleTrekkvedtak(
        xmlContent: String,
        currentDate: LocalDate,
        replyToQueue: Boolean = true,
    ): Trekk {
        return runCatching {
            val trekk = unmarshalTrekk(xmlContent)

            val typeKjoring = trekk.typeKjoring
            val trekkRequestList = trekk.trekkRequest.distinctBy { it.trekkvedtakId }

            logger.info { "Starter behandling av ${trekkRequestList.size} trekkvedtak." }

            val fnrSet = trekkRequestList.map { it.offnr }.toSet()
            val toDate = currentDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth())
            val ytelseVedtakMap = hentYtelsesVedtak(fnrSet, currentDate, toDate)

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
                    TypeKjoring.PERI -> producer.send(replyXML, batchReplyQueue, Metrics.mqBatchReplyMetricCounter)
                    else -> producer.send(replyXML, replyQueue, Metrics.mqReplyMetricCounter)
                }
            }

            response
        }.onFailure { exception ->
            logger.error(exception) { "Behandling av trekkvedtak feilet" }
            throw exception
        }.getOrThrow()
    }

    private fun hentYtelsesVedtak(
        fnrSet: Set<String>,
        fromDate: LocalDate,
        toDate: LocalDate,
    ): Map<String, List<ArenaVedtak>> {
        return runCatching {
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
                }

            soapArenaRequestCounter.inc()
            val response = arenaClientService.finnYtelseVedtakListe(request)
            soapArenaResponseCounter.inc()

            response.personYtelseListe.associateBy({ it.ident }, { it.sakListe.flatMap { sak -> sak.mapToAreanVedtak() } })
        }.onFailure { exception ->
            soapArenaErrorCounter.inc()
            throw exception
        }.getOrThrow()
    }

    private fun Sak.mapToAreanVedtak(): List<ArenaVedtak> {
        val tema = this.tema.kodeverksRef
        return this.vedtakListe.map { vedtak ->
            ArenaVedtak().apply {
                dagsats = vedtak.dagsats.toBigDecimal().setScale(SUM_SCALE, RoundingMode.HALF_UP)
                rettighetType = vedtak.rettighetstype.kodeverksRef
                this.tema = tema
                vedtaksperiode =
                    no.nav.maskinelletrekk.trekk.v1.Periode().apply {
                        fom = vedtak.vedtaksperiode.fom
                        tom = vedtak.vedtaksperiode.tom
                    }
            }
        }
    }
}
