package no.nav.sokos.trekk.soap

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

import no.nav.sokos.trekk.config.PropertiesConfig
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.YtelseVedtakV1
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeRequest
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeResponse

class ArenaClientServiceTest : FunSpec({
    val mockSoapClient = mockk<YtelseVedtakV1>()
    val soapProperties =
        PropertiesConfig.SoapProperties(
            ytelsevedtakV1EndpointUrl = "http://example.com",
            stsUrl = "http://sts.example.com",
            serviceUsername = "user",
            servicePassword = "password",
        )

    val arenaClientService =
        ArenaClientService(soapProperties).apply {
            val field = this::class.java.getDeclaredField("ytelsesVedtakSoapClient")
            field.isAccessible = true
            field.set(this, mockSoapClient)
        }

    test("should log and call finnYtelseVedtakListe") {
        val request = FinnYtelseVedtakListeRequest()
        val response = FinnYtelseVedtakListeResponse()

        every { mockSoapClient.finnYtelseVedtakListe(request) } returns response

        val result = arenaClientService.finnYtelseVedtakListe(request)

        result shouldBe response
        verify { mockSoapClient.finnYtelseVedtakListe(request) }
    }
})
