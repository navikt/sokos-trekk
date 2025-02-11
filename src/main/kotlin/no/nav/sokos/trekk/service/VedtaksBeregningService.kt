package no.nav.sokos.trekk.service

import java.math.BigDecimal
import java.math.RoundingMode

import mu.KotlinLogging

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak
import no.nav.maskinelletrekk.trekk.v1.Beslutning
import no.nav.maskinelletrekk.trekk.v1.System
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest
import no.nav.maskinelletrekk.trekk.v1.TrekkResponse
import no.nav.maskinelletrekk.trekk.v1.Trekkalternativ

const val SUM_SCALE = 2
const val FAKTOR_MND = 21.67

private val logger = KotlinLogging.logger { }

class VedtaksBeregningService(
    private val arenaVedtakMap: Map<String, List<ArenaVedtak>>,
) : (TrekkRequest) -> TrekkResponse {
    override fun invoke(request: TrekkRequest): TrekkResponse {
        val trekkvedtakId = request.trekkvedtakId
        logger.info { "Starter beregning av trekkvedtak[trekkvedtakId:$trekkvedtakId]" }

        val arenaVedtakList = finnArenaYtelsesvedtakForBruker(request)
        val sumArena = kalkulerSumArena(arenaVedtakList)
        val sumOS = request.getTotalSatsOS()
        val trekkSats = request.trekkSats
        val system = request.system
        val beslutning =
            when (request.trekkalt) {
                Trekkalternativ.SALP, Trekkalternativ.LOPP -> besluttProsenttrekk(sumArena, sumOS)
                else -> besluttLopendeOgSaldotrekk(sumArena, sumOS, trekkSats, system)
            }

        return TrekkResponse().apply {
            this.trekkvedtakId = trekkvedtakId
            this.totalSatsArena = sumArena
            this.totalSatsOS = sumOS
            this.beslutning = beslutning
            this.system = system
            this.vedtak.addAll(arenaVedtakList)
        }
    }

    private fun finnArenaYtelsesvedtakForBruker(request: TrekkRequest): List<ArenaVedtak> {
        val arenaVedtakList = arenaVedtakMap[request.offnr].orEmpty().toList()

        logger.info { "Funnet ${arenaVedtakList.size} Arena-vedtak for trekkvedtak[trekkvedtakId: ${request.trekkvedtakId}]" }
        return arenaVedtakList
    }

    private fun kalkulerSumArena(arenaVedtakList: List<ArenaVedtak>): BigDecimal {
        return arenaVedtakList
            .map { it.dagsats }
            .fold(BigDecimal.ZERO) { acc, dagsats -> acc + dagsats }
            .multiply(FAKTOR_MND.toBigDecimal())
            .setScale(SUM_SCALE, RoundingMode.HALF_UP)
    }

    private fun besluttProsenttrekk(
        sumArena: BigDecimal,
        sumOs: BigDecimal,
    ): Beslutning {
        return when {
            sumArena > BigDecimal.ZERO -> Beslutning.ABETAL
            sumOs > BigDecimal.ZERO && sumArena > BigDecimal.ZERO -> Beslutning.BEGGE
            sumOs > BigDecimal.ZERO -> Beslutning.OS
            else -> Beslutning.INGEN
        }
    }

    private fun besluttLopendeOgSaldotrekk(
        sumArena: BigDecimal,
        sumOs: BigDecimal,
        trekkSats: BigDecimal,
        system: System,
    ): Beslutning {
        return when {
            (system == System.J && sumArena >= trekkSats && sumArena != BigDecimal.ZERO) || (sumArena >= sumOs && sumArena > BigDecimal.ZERO) -> Beslutning.ABETAL
            sumOs > sumArena -> Beslutning.OS
            else -> Beslutning.INGEN
        }
    }
}
