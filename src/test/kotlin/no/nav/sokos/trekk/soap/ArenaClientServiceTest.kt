package no.nav.sokos.trekk.soap

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.cxf.BusFactory
import org.apache.cxf.bus.extension.ExtensionManagerBus
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.rt.security.SecurityConstants
import org.apache.cxf.transport.http.HTTPConduit
import org.apache.cxf.ws.policy.PolicyEngine
import org.apache.cxf.ws.security.trust.STSClient

import no.nav.sokos.trekk.config.PropertiesConfig
import no.nav.sokos.trekk.config.getYtelsesVedtakSoapClient
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.YtelseVedtakV1
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeRequest
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeResponse

class ArenaClientServiceTest :
    FunSpec({
        val mockSoapClient = mockk<YtelseVedtakV1>()
        val soapProperties =
            PropertiesConfig.SoapProperties(
                ytelsevedtakV1EndpointUrl = "http://example.com",
                stsUrl = "http://sts.example.com",
                serviceUsername = "user",
                servicePassword = "password",
            )

        val arenaClientService: ArenaClientService by lazy {
            ArenaClientService(soapProperties, mockSoapClient)
        }

        test("should log and call finnYtelseVedtakListe") {
            val request = FinnYtelseVedtakListeRequest()
            val response = FinnYtelseVedtakListeResponse()

            every { mockSoapClient.finnYtelseVedtakListe(request) } returns response

            val result = arenaClientService.finnYtelseVedtakListe(request)

            result shouldBe response
            verify { mockSoapClient.finnYtelseVedtakListe(request) }
        }

        test("should create a real CXF client with HTTP transport and STS policies without calling a backend") {
            val previousBus = BusFactory.getThreadDefaultBus(false)
            val bus = ExtensionManagerBus()
            BusFactory.setThreadDefaultBus(bus)
            try {
                val port =
                    getYtelsesVedtakSoapClient(
                        soapProperties.copy(
                            ytelsevedtakV1EndpointUrl = "https://arena.invalid/ytelsevedtak",
                            stsUrl = "https://sts.invalid/token",
                        ),
                    )
                val client = ClientProxy.getClient(port)
                try {
                    client.conduit.shouldBeInstanceOf<HTTPConduit>()
                    client.bus.getExtension(PolicyEngine::class.java).shouldNotBeNull()
                    val sts = client.requestContext[SecurityConstants.STS_CLIENT].shouldBeInstanceOf<STSClient>()
                    sts.location shouldBe "https://sts.invalid/token"
                } finally {
                    client.destroy()
                }
            } finally {
                bus.shutdown(true)
                BusFactory.setThreadDefaultBus(previousBus)
            }
        }
    })
