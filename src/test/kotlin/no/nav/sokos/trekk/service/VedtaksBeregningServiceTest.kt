package no.nav.sokos.trekk.service

import java.math.BigDecimal

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.bigdecimal.shouldBeGreaterThan
import io.kotest.matchers.shouldBe

import no.nav.maskinelletrekk.trekk.v1.Beslutning
import no.nav.maskinelletrekk.trekk.v1.System
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest
import no.nav.maskinelletrekk.trekk.v1.Trekkalternativ
import no.nav.sokos.trekk.TestUtil.readFromResource
import no.nav.sokos.trekk.TestUtil.unmarshalFinnYtelseVedtakListeResponse

private const val FNR = "12312312312"
private const val YTELSEVEDTAK_1_RESPONSE_XML = "ytelseVedtakResponse_1.xml"
private const val YTELSEVEDTAK_2_RESPONSE_XML = "ytelseVedtakResponse_2.xml"
private const val YTELSEVEDTAK_3_RESPONSE_XML = "ytelseVedtakResponse_3.xml"
private const val DAGSATS = "2167.00"

class VedtaksBeregningServiceTest : FunSpec({
    test("skal besluttLopendeOgSaldotrekk - TrekkRequest: trekkalt=LOPD, system=J, sumArena >= trekkSats, sumArena > 0 returnere TrekkResponse: beslutning=Abetal") {
        val ytelseVedtakListeResponse = YTELSEVEDTAK_1_RESPONSE_XML.readFromResource().unmarshalFinnYtelseVedtakListeResponse()
        val arenaVedtakMap = ytelseVedtakListeResponse.mapToAreanVedtak()

        val request =
            TrekkRequest().apply {
                trekkvedtakId = 1
                offnr = FNR
                trekkalt = Trekkalternativ.LOPD
                system = System.J
                trekkSats = DAGSATS.toBigDecimal()
                totalSatsOS = DAGSATS.toBigDecimal()
            }
        val response = VedtaksBeregningService(arenaVedtakMap).invoke(request)
        response.trekkvedtakId shouldBe 1
        response.system shouldBe System.J
        response.beslutning shouldBe Beslutning.ABETAL
        response.totalSatsArena shouldBe "6501.00".toBigDecimal()
        response.totalSatsOS shouldBe DAGSATS.toBigDecimal()
        response.totalSatsArena shouldBeGreaterThan request.trekkSats
        response.vedtak.size shouldBe 3
    }

    test("skal besluttLopendeOgSaldotrekk - TrekkRequest: trekkalt=LOPD, system=N, sumArena > sumOs, sumArena > 0 returnere TrekkResponse: beslutning=OS") {
        val ytelseVedtakListeResponse = YTELSEVEDTAK_2_RESPONSE_XML.readFromResource().unmarshalFinnYtelseVedtakListeResponse()
        val arenaVedtakMap = ytelseVedtakListeResponse.mapToAreanVedtak()

        val request =
            TrekkRequest().apply {
                trekkvedtakId = 1
                offnr = FNR
                trekkalt = Trekkalternativ.LOPD
                system = System.N
                trekkSats = DAGSATS.toBigDecimal().add(2.toBigDecimal())
                totalSatsOS = DAGSATS.toBigDecimal().subtract(BigDecimal.ONE)
            }
        val response = VedtaksBeregningService(arenaVedtakMap).invoke(request)
        response.trekkvedtakId shouldBe 1
        response.system shouldBe System.N
        response.beslutning shouldBe Beslutning.ABETAL
        response.totalSatsArena shouldBe DAGSATS.toBigDecimal()
        response.totalSatsOS shouldBe DAGSATS.toBigDecimal().subtract(BigDecimal.ONE)
        response.totalSatsArena shouldBeGreaterThan response.totalSatsOS
        response.vedtak.size shouldBe 1
    }

    test("skal besluttLopendeOgSaldotrekk - TrekkRequest: trekkalt=LOPD, system=J, sumOs > sumArena returnere TrekkResponse: beslutning=OS") {
        val ytelseVedtakListeResponse = YTELSEVEDTAK_2_RESPONSE_XML.readFromResource().unmarshalFinnYtelseVedtakListeResponse()
        val arenaVedtakMap = ytelseVedtakListeResponse.mapToAreanVedtak()

        val request =
            TrekkRequest().apply {
                trekkvedtakId = 1
                offnr = FNR
                trekkalt = Trekkalternativ.LOPD
                system = System.J
                trekkSats = DAGSATS.toBigDecimal().add(2.toBigDecimal())
                totalSatsOS = DAGSATS.toBigDecimal().add(BigDecimal.ONE)
            }
        val response = VedtaksBeregningService(arenaVedtakMap).invoke(request)
        response.trekkvedtakId shouldBe 1
        response.system shouldBe System.J
        response.beslutning shouldBe Beslutning.OS
        response.totalSatsArena shouldBe DAGSATS.toBigDecimal()
        response.totalSatsOS shouldBe DAGSATS.toBigDecimal().add(BigDecimal.ONE)
        response.totalSatsOS shouldBeGreaterThan response.totalSatsArena
        response.vedtak.size shouldBe 1
    }

    test("skal besluttLopendeOgSaldotrekk - TrekkRequest: trekkalt=LOPD, system=null, sumOs > sumArena returnere TrekkResponse: beslutning=OS") {
        val ytelseVedtakListeResponse = YTELSEVEDTAK_2_RESPONSE_XML.readFromResource().unmarshalFinnYtelseVedtakListeResponse()
        val arenaVedtakMap = ytelseVedtakListeResponse.mapToAreanVedtak()

        val request =
            TrekkRequest().apply {
                trekkvedtakId = 1
                offnr = FNR
                trekkalt = Trekkalternativ.LOPD
                system = null
                trekkSats = DAGSATS.toBigDecimal().add(2.toBigDecimal())
                totalSatsOS = DAGSATS.toBigDecimal().add(BigDecimal.ONE)
            }
        val response = VedtaksBeregningService(arenaVedtakMap).invoke(request)
        response.trekkvedtakId shouldBe 1
        response.system shouldBe null
        response.beslutning shouldBe Beslutning.OS
        response.totalSatsArena shouldBe DAGSATS.toBigDecimal()
        response.totalSatsOS shouldBe DAGSATS.toBigDecimal().add(BigDecimal.ONE)
        response.totalSatsOS shouldBeGreaterThan response.totalSatsArena
        response.vedtak.size shouldBe 1
    }

    test("skal besluttLopendeOgSaldotrekk - TrekkRequest: trekkalt=LOPD, system=N, sumOs < sumArena returnere TrekkResponse: beslutning=INGEN") {
        val ytelseVedtakListeResponse = YTELSEVEDTAK_3_RESPONSE_XML.readFromResource().unmarshalFinnYtelseVedtakListeResponse()
        val arenaVedtakMap = ytelseVedtakListeResponse.mapToAreanVedtak()

        val request =
            TrekkRequest().apply {
                trekkvedtakId = 1
                offnr = FNR
                trekkalt = Trekkalternativ.LOPD
                system = System.N
                trekkSats = DAGSATS.toBigDecimal()
                totalSatsOS = BigDecimal.ZERO
            }
        val response = VedtaksBeregningService(arenaVedtakMap).invoke(request)
        response.trekkvedtakId shouldBe 1
        response.system shouldBe System.N
        response.beslutning shouldBe Beslutning.INGEN
        response.totalSatsArena shouldBe "0.00".toBigDecimal()
        response.totalSatsOS shouldBe BigDecimal.ZERO
        response.vedtak.size shouldBe 1
    }

    test("skal besluttProsenttrekk - TrekkRequest: trekkalt=LOPP, system=N, sumArena > 0, sumOs > 0 returnere TrekkResponse: beslutning=BEGGE") {
        val ytelseVedtakListeResponse = YTELSEVEDTAK_2_RESPONSE_XML.readFromResource().unmarshalFinnYtelseVedtakListeResponse()
        val arenaVedtakMap = ytelseVedtakListeResponse.mapToAreanVedtak()

        val request =
            TrekkRequest().apply {
                trekkvedtakId = 1
                offnr = FNR
                trekkalt = Trekkalternativ.LOPP
                system = System.N
                trekkSats = DAGSATS.toBigDecimal()
                totalSatsOS = DAGSATS.toBigDecimal().subtract(BigDecimal.ONE)
            }
        val response = VedtaksBeregningService(arenaVedtakMap).invoke(request)
        response.trekkvedtakId shouldBe 1
        response.system shouldBe System.N
        response.beslutning shouldBe Beslutning.BEGGE
        response.totalSatsArena shouldBe DAGSATS.toBigDecimal()
        response.totalSatsOS shouldBe DAGSATS.toBigDecimal().subtract(BigDecimal.ONE)
        response.vedtak.size shouldBe 1
    }

    test("skal besluttProsenttrekk - TrekkRequest: trekkalt=LOPP, system=J, sumOs = 0, sumArena > 0 returnere TrekkResponse: beslutning=ABETAL") {
        val ytelseVedtakListeResponse = YTELSEVEDTAK_2_RESPONSE_XML.readFromResource().unmarshalFinnYtelseVedtakListeResponse()
        val arenaVedtakMap = ytelseVedtakListeResponse.mapToAreanVedtak()

        val request =
            TrekkRequest().apply {
                trekkvedtakId = 1
                offnr = FNR
                trekkalt = Trekkalternativ.LOPP
                system = System.J
                trekkSats = BigDecimal.ZERO
                totalSatsOS = BigDecimal.ZERO
            }
        val response = VedtaksBeregningService(arenaVedtakMap).invoke(request)
        response.trekkvedtakId shouldBe 1
        response.system shouldBe System.J
        response.beslutning shouldBe Beslutning.ABETAL
        response.totalSatsArena shouldBe DAGSATS.toBigDecimal()
        response.totalSatsOS shouldBe BigDecimal.ZERO
        response.vedtak.size shouldBe 1
    }

    test("skal besluttProsenttrekk - TrekkRequest: trekkalt=LOPP, system=J, sumOs > 0, sumArena = 0 returnere TrekkResponse: beslutning=OS") {
        val ytelseVedtakListeResponse = YTELSEVEDTAK_3_RESPONSE_XML.readFromResource().unmarshalFinnYtelseVedtakListeResponse()
        val arenaVedtakMap = ytelseVedtakListeResponse.mapToAreanVedtak()

        val request =
            TrekkRequest().apply {
                trekkvedtakId = 1
                offnr = FNR
                trekkalt = Trekkalternativ.LOPP
                system = System.J
                trekkSats = DAGSATS.toBigDecimal()
                totalSatsOS = DAGSATS.toBigDecimal()
            }
        val response = VedtaksBeregningService(arenaVedtakMap).invoke(request)
        response.trekkvedtakId shouldBe 1
        response.system shouldBe System.J
        response.beslutning shouldBe Beslutning.OS
        response.totalSatsArena shouldBe "0.00".toBigDecimal()
        response.totalSatsOS shouldBe DAGSATS.toBigDecimal()
        response.vedtak.size shouldBe 1
    }

    test("skal besluttProsenttrekk - TrekkRequest: trekkalt=LOPP, system=J, sumOs = 0, sumArena = 0 returnere TrekkResponse: beslutning=INGEN") {
        val ytelseVedtakListeResponse = YTELSEVEDTAK_3_RESPONSE_XML.readFromResource().unmarshalFinnYtelseVedtakListeResponse()
        val arenaVedtakMap = ytelseVedtakListeResponse.mapToAreanVedtak()

        val request =
            TrekkRequest().apply {
                trekkvedtakId = 1
                offnr = FNR
                trekkalt = Trekkalternativ.LOPP
                system = System.J
                trekkSats = DAGSATS.toBigDecimal()
                totalSatsOS = BigDecimal.ZERO
            }
        val response = VedtaksBeregningService(arenaVedtakMap).invoke(request)
        response.trekkvedtakId shouldBe 1
        response.system shouldBe System.J
        response.beslutning shouldBe Beslutning.INGEN
        response.totalSatsArena shouldBe "0.00".toBigDecimal()
        response.totalSatsOS shouldBe BigDecimal.ZERO
        response.vedtak.size shouldBe 1
    }
})
