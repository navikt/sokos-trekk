package no.nav.sokos.trekk.soap

import mu.KotlinLogging

import no.nav.sokos.trekk.config.NAMESPACE
import no.nav.sokos.trekk.config.PropertiesConfig
import no.nav.sokos.trekk.config.TEAM_LOGS_MARKER
import no.nav.sokos.trekk.config.getYtelsesVedtakSoapClient
import no.nav.sokos.trekk.util.JaxbUtils.toXml
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.YtelseVedtakV1
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeRequest
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeResponse

private val logger = KotlinLogging.logger {}

class ArenaClientService(
    private val soapProperties: PropertiesConfig.SoapProperties = PropertiesConfig.SoapProperties(),
    private val ytelseVedtakV1: YtelseVedtakV1 = getYtelsesVedtakSoapClient(soapProperties),
) {
    fun finnYtelseVedtakListe(request: FinnYtelseVedtakListeRequest): FinnYtelseVedtakListeResponse {
        logger.info(marker = TEAM_LOGS_MARKER) { "FinnYtelseVedtakListeRequest: ${request.toXml(NAMESPACE)}" }

        val response = ytelseVedtakV1.finnYtelseVedtakListe(request)
        logger.info(marker = TEAM_LOGS_MARKER) { "FinnYtelseVedtakListeResponse: ${response.toXml(NAMESPACE)}" }

        return response
    }
}
