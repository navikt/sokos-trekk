package no.nav.sokos.trekk.soap

import javax.xml.namespace.QName

import mu.KotlinLogging
import org.apache.cxf.Bus
import org.apache.cxf.BusFactory
import org.apache.cxf.bus.CXFBusFactory

import no.nav.common.cxf.CXFClient
import no.nav.common.cxf.StsConfig
import no.nav.sokos.trekk.config.PropertiesConfig
import no.nav.sokos.trekk.config.STSTokenProviderConfig
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.YtelseVedtakV1
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeRequest
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeResponse

private const val NAMESPACE = "http://nav.no/tjeneste/virksomhet/ytelseVedtak/v1"
private const val WSDL_URL = "wsdl/no/nav/tjeneste/virksomhet/ytelseVedtak/v1/YtelseVedtakV1.wsdl"
private const val YTELSE_VEDTAK_SERVICE_NAME = "YtelseVedtak_v1"
private const val YTELSE_VEDTAK_ENDPOINT_NAME = "YtelseVedtak_v1Port"

private val secureLogger = KotlinLogging.logger("secureLogger")

class ArenaClientService(
    private val arenaUrl: String = PropertiesConfig.SoapProperties().ytelsevedtakV1EndpointUrl,
    private val stsConfig: StsConfig = STSTokenProviderConfig().stsConfig(),
//    stsClient: STSClient =
//        STSClientConfig.stsClient(
//            PropertiesConfig.SoapProperties().stsUrl,
//            Pair(
//                PropertiesConfig.ServiceUserProperties().serviceUsername,
//                PropertiesConfig.ServiceUserProperties().servicePassword,
//            ),
//        ),
) {
    //    private val loggingFeature =
//        LoggingFeature().also {
//            it.setSender { event -> secureLogger.info("SOAP melding -> ${LogMessageFormatter.format(event)}") }
//        }

    //    private val ytelsesVedtakSoapClient =
//        JaxWsProxyFactoryBean().apply {
//            address = "$arenaUrl/$YTELSE_VEDTAK_SERVICE_NAME"
//            wsdlURL = WSDL_URL
//            serviceName = QName(NAMESPACE, YTELSE_VEDTAK_SERVICE_NAME)
//            endpointName = QName(NAMESPACE, YTELSE_VEDTAK_ENDPOINT_NAME)
//            serviceClass = YtelseVedtakV1::class.java
//            features = listOf(WSAddressingFeature(), loggingFeature)
//            outInterceptors.add(CallIdInterceptor { ULID.randomULID() })
//        }.create(YtelseVedtakV1::class.java).also { stsClient.configureFor(it) }
    init {
        // Initialize and set the default CXF bus
        val busFactory = CXFBusFactory()
        val bus: Bus = busFactory.createBus()
        BusFactory.setDefaultBus(bus)
    }

    private val ytelsesVedtakSoapClient: YtelseVedtakV1 =
        CXFClient(YtelseVedtakV1::class.java)
            .wsdl(WSDL_URL)
            .serviceName(QName(NAMESPACE, YTELSE_VEDTAK_SERVICE_NAME))
            .endpointName(QName(NAMESPACE, YTELSE_VEDTAK_ENDPOINT_NAME))
            .withHandler(SoapMessageHandler())
            .address(arenaUrl)
            .configureStsForSubject(stsConfig)
            .build()

    fun finnYtelseVedtakListe(request: FinnYtelseVedtakListeRequest): FinnYtelseVedtakListeResponse {
        secureLogger.info { "FinnYtelseVedtakListeRequest: $request" }
        return ytelsesVedtakSoapClient.finnYtelseVedtakListe(request)
    }
}
