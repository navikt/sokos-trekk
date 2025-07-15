package no.nav.sokos.trekk.soap

import javax.xml.namespace.QName

import mu.KotlinLogging
import org.apache.cxf.ext.logging.LoggingFeature
import org.apache.cxf.ext.logging.event.LogMessageFormatter
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.ws.addressing.WSAddressingFeature
import ulid.ULID

import no.nav.sokos.trekk.config.PropertiesConfig
import no.nav.sokos.trekk.config.ServiceUserConfig
import no.nav.sokos.trekk.config.wrapInStsClient
import no.nav.sokos.trekk.util.JaxbUtils.toXml
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.YtelseVedtakV1
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeRequest
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeResponse

private const val NAMESPACE = "http://nav.no/tjeneste/virksomhet/ytelseVedtak/v1"
private const val WSDL_URL = "wsdl/no/nav/tjeneste/virksomhet/ytelseVedtak/v1/YtelseVedtakV1.wsdl"
private const val YTELSE_VEDTAK_SERVICE_NAME = "YtelseVedtak_v1"
private const val YTELSE_VEDTAK_ENDPOINT_NAME = "YtelseVedtak_v1Port"

private val secureLogger = KotlinLogging.logger("secureLogger")
private val logger = KotlinLogging.logger {}

class ArenaClientService(
    private val soapProperties: PropertiesConfig.SoapProperties = PropertiesConfig.SoapProperties(),
) {
    private val loggingFeature =
        LoggingFeature()
            .apply {
                setVerbose(true)
                setPrettyLogging(true)
            }.also {
                it.setSender { event -> logger.debug { "SOAP melding -> ${LogMessageFormatter.format(event)}" } }
            }

    private val ytelsesVedtakSoapClient: YtelseVedtakV1 =
        JaxWsProxyFactoryBean()
            .apply {
                address = soapProperties.ytelsevedtakV1EndpointUrl
                wsdlURL = WSDL_URL
                serviceName = QName(NAMESPACE, YTELSE_VEDTAK_SERVICE_NAME)
                endpointName = QName(NAMESPACE, YTELSE_VEDTAK_ENDPOINT_NAME)
                serviceClass = YtelseVedtakV1::class.java
                features = listOf(WSAddressingFeature(), loggingFeature)
                outInterceptors.add(CallIdInterceptor { ULID.randomULID() })
            }.wrapInStsClient(
                soapProperties.stsUrl,
                ServiceUserConfig(
                    soapProperties.serviceUsername,
                    soapProperties.servicePassword,
                ),
                true,
            )

    fun finnYtelseVedtakListe(request: FinnYtelseVedtakListeRequest): FinnYtelseVedtakListeResponse {
        secureLogger.info { "FinnYtelseVedtakListeRequest: ${request.toXml(NAMESPACE)}" }

        val response = ytelsesVedtakSoapClient.finnYtelseVedtakListe(request)
        secureLogger.info { "FinnYtelseVedtakListeResponse: ${response.toXml(NAMESPACE)}" }

        return response
    }
}
