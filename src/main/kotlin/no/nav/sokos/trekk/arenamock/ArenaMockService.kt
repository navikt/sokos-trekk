package no.nav.sokos.trekk.arenamock

import java.io.ByteArrayInputStream
import java.math.RoundingMode
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.YearMonth

import jakarta.xml.bind.JAXB
import mu.KotlinLogging

import no.nav.maskinelletrekk.arenamock.v1.ArenaMockData
import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak
import no.nav.maskinelletrekk.trekk.v1.Periode
import no.nav.sokos.trekk.service.SUM_SCALE
import no.nav.sokos.trekk.util.Utils.toLocalDate
import no.nav.sokos.trekk.util.Utils.toXMLGregorianCalendar

private val logger = KotlinLogging.logger {}

object ArenaMockService {
    var mockDataXml: String? = null
    var kjoreDato: LocalDate? = null
    val areanaMockDataMap: MutableMap<String, List<ArenaVedtak>> = mutableMapOf()

    fun lagreArenaMockData(xmlContent: String) {
        val arenaMockData = JAXB.unmarshal(ByteArrayInputStream(xmlContent.toByteArray(StandardCharsets.UTF_8)), ArenaMockData::class.java)
        mockDataXml = xmlContent
        kjoreDato = arenaMockData.kjoreDato?.toGregorianCalendar()?.toZonedDateTime()?.toLocalDate()
        areanaMockDataMap.clear()
        areanaMockDataMap.putAll(
            arenaMockData.personYtelse.flatMap { personYtelse ->
                personYtelse.sak.flatMap { sak ->
                    sak.vedtak.map { vedtak ->
                        personYtelse.ident to
                            ArenaVedtak().apply {
                                dagsats = vedtak.dagsats.toBigDecimal().setScale(SUM_SCALE, RoundingMode.HALF_UP)
                                rettighetType = vedtak.rettighetstype.value()
                                tema = sak.tema.value()
                                vedtaksperiode =
                                    Periode().apply {
                                        fom = vedtak.vedtaksperiode.fom
                                        tom = vedtak.vedtaksperiode.tom
                                    }
                            }
                    }
                }
            }.groupBy({ it.first }, { it.second }),
        )
    }

    fun hentYtelsesVedtak(
        fnrSet: Set<String>,
        fromDate: LocalDate,
        toDate: LocalDate,
    ): Map<String, List<ArenaVedtak>> {
        logger.info("[ARENA-MOCK]: Bruk ArenaMockService")
        val periode: Periode =
            if (kjoreDato != null) {
                logger.info("[ARENA-MOCK]: Kjøredato er satt til $kjoreDato")
                Periode().apply {
                    fom = YearMonth.from(kjoreDato).plusMonths(1).atDay(1).toXMLGregorianCalendar()
                    tom = YearMonth.from(kjoreDato).plusMonths(1).atEndOfMonth().toXMLGregorianCalendar()
                }
            } else {
                Periode().apply {
                    fom = fromDate.toXMLGregorianCalendar()
                    tom = toDate.toXMLGregorianCalendar()
                }
            }
        if (areanaMockDataMap.isNotEmpty()) {
            return fnrSet.mapNotNull { fnr ->
                areanaMockDataMap[fnr]?.filter { vedtak -> erInnenforPeriode(vedtak.vedtaksperiode, periode) }
                    ?.takeIf { it.isNotEmpty() }
                    ?.also { logger.info { "[ARENA-MOCK]: Hentet ${it.size} Arena-vedtak for bruker $fnr og periode ${periode.fom.toLocalDate()} til ${periode.tom.toLocalDate()}" } }
            }.associateBy { it.first().rettighetType }
        }

        logger.warn("[ARENA-MOCK]: Mangler testdata!")
        return emptyMap()
    }

    private fun erInnenforPeriode(
        arenaVedtaksPeriode: Periode,
        requestPeriode: Periode,
    ): Boolean {
        val fom = arenaVedtaksPeriode.fom.toLocalDate()
        val tom = arenaVedtaksPeriode.tom?.toLocalDate()
        val requestFom = requestPeriode.fom.toLocalDate()
        val requestTom = requestPeriode.tom.toLocalDate()

        return tom?.let {
            (fom.isBefore(requestFom) && it.isAfter(requestFom)) ||
                (it.isAfter(requestFom) || it.isEqual(requestFom)) && (it.isBefore(requestTom) || it.isEqual(requestTom)) ||
                (fom.isAfter(requestFom) || fom.isEqual(requestFom)) && (fom.isBefore(requestTom) || fom.isEqual(requestTom))
        } ?: (fom.isBefore(requestTom) || fom.isEqual(requestTom))
    }
}
