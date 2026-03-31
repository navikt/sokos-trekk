package no.nav.sokos.trekk.arenamock

import java.io.ByteArrayInputStream
import java.math.RoundingMode
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.atomic.AtomicReference

import jakarta.xml.bind.JAXB
import mu.KotlinLogging

import no.nav.maskinelletrekk.arenamock.v1.ArenaMockData
import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak
import no.nav.maskinelletrekk.trekk.v1.Periode
import no.nav.sokos.trekk.service.SUM_SCALE
import no.nav.sokos.trekk.util.Utils.toLocalDate
import no.nav.sokos.trekk.util.Utils.toXMLGregorianCalendar

private val logger = KotlinLogging.logger {}

private data class ArenaMockSnapshot(
    val mockDataXml: String?,
    val kjoreDato: LocalDate?,
    val arenaMockDataMap: Map<String, List<ArenaVedtak>>,
)

object ArenaMockService {
    private val snapshotRef =
        AtomicReference(
            ArenaMockSnapshot(
                mockDataXml = null,
                kjoreDato = null,
                arenaMockDataMap = emptyMap(),
            ),
        )

    // Keep read access with same property names
    val mockDataXml: String?
        get() = snapshotRef.get().mockDataXml

    val kjoreDato: LocalDate?
        get() = snapshotRef.get().kjoreDato

    val arenaMockDataMap: Map<String, List<ArenaVedtak>>
        get() = snapshotRef.get().arenaMockDataMap

    fun lagreArenaMockData(xmlContent: String) {
        val arenaMockData =
            JAXB.unmarshal(
                ByteArrayInputStream(xmlContent.toByteArray(StandardCharsets.UTF_8)),
                ArenaMockData::class.java,
            )

        val nyKjoreDato =
            arenaMockData.kjoreDato
                ?.toGregorianCalendar()
                ?.toZonedDateTime()
                ?.toLocalDate()

        val nyDataMap =
            arenaMockData.personYtelse
                .flatMap { personYtelse ->
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
                }.groupBy({ it.first }, { it.second })
                .mapValues { (_, vedtakListe) -> vedtakListe.toList() }

        // Single atomic publish to avoid readers seeing partial state
        snapshotRef.set(
            ArenaMockSnapshot(
                mockDataXml = xmlContent,
                kjoreDato = nyKjoreDato,
                arenaMockDataMap = nyDataMap,
            ),
        )
    }

    fun hentYtelsesVedtak(
        fnrSet: Set<String>,
        fromDate: LocalDate,
        toDate: LocalDate,
    ): Map<String, List<ArenaVedtak>> {
        logger.info("[ARENA-MOCK]: Bruk ArenaMockService")
        val snapshot = snapshotRef.get()

        val periode: Periode =
            if (snapshot.kjoreDato != null) {
                logger.info("[ARENA-MOCK]: Kjøredato er satt til ${snapshot.kjoreDato}")
                Periode().apply {
                    fom =
                        YearMonth
                            .from(snapshot.kjoreDato)
                            .plusMonths(1)
                            .atDay(1)
                            .toXMLGregorianCalendar()
                    tom =
                        YearMonth
                            .from(snapshot.kjoreDato)
                            .plusMonths(1)
                            .atEndOfMonth()
                            .toXMLGregorianCalendar()
                }
            } else {
                Periode().apply {
                    fom = fromDate.toXMLGregorianCalendar()
                    tom = toDate.toXMLGregorianCalendar()
                }
            }

        if (snapshot.arenaMockDataMap.isEmpty()) {
            logger.warn("[ARENA-MOCK]: Mangler testdata!")
            return emptyMap()
        }

        return fnrSet
            .mapNotNull { fnr ->
                snapshot.arenaMockDataMap[fnr]
                    ?.filter { vedtak -> erInnenforPeriode(vedtak.vedtaksperiode, periode) }
                    ?.takeIf { it.isNotEmpty() }
                    ?.also {
                        logger.info {
                            "[ARENA-MOCK]: Hentet ${it.size} Arena-vedtak for bruker $fnr og periode ${periode.fom.toLocalDate()} til ${periode.tom.toLocalDate()}"
                        }
                    }?.let { fnr to it }
            }.toMap()
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
                (it.isAfter(requestFom) || it.isEqual(requestFom)) &&
                (it.isBefore(requestTom) || it.isEqual(requestTom)) ||
                (fom.isAfter(requestFom) || fom.isEqual(requestFom)) &&
                (fom.isBefore(requestTom) || fom.isEqual(requestTom))
        } ?: (fom.isBefore(requestTom) || fom.isEqual(requestTom))
    }
}
