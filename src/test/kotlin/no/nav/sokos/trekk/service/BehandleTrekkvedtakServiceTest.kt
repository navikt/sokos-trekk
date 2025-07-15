package no.nav.sokos.trekk.service

import java.time.LocalDate

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

import no.nav.maskinelletrekk.trekk.v1.Beslutning
import no.nav.sokos.trekk.TestUtil.readFromResource
import no.nav.sokos.trekk.TestUtil.unmarshalFinnYtelseVedtakListeResponse
import no.nav.sokos.trekk.mq.JmsProducerService
import no.nav.sokos.trekk.soap.ArenaClientService

private const val TREKK_V1_REQUEST_1_XML = "trekk_v1_request_1.xml"
private const val TREKK_V1_REQUEST_2_XML = "trekk_v1_request_2.xml"
private const val YTELSEVEDTAK_RESPONSE_XML = "ytelseVedtakResponse.xml"

class BehandleTrekkvedtakServiceTest :
    BehaviorSpec({

        val arenaClientService = mockk<ArenaClientService>()
        val producer = mockk<JmsProducerService>()
        val behandleTrekkvedtakService: BehandleTrekkvedtakService by lazy {
            BehandleTrekkvedtakService(arenaClientService, producer)
        }

        afterEach {
            clearAllMocks()
        }

        given("skal beslutte OS dersom sum OS er større enn SumArena") {
            val ytelseVedtakListeResponse = YTELSEVEDTAK_RESPONSE_XML.readFromResource().unmarshalFinnYtelseVedtakListeResponse()
            every { arenaClientService.finnYtelseVedtakListe(any()) } returns ytelseVedtakListeResponse
            every { producer.send(any(), any(), any()) } returns Unit

            When("behandleTrekkvedtak is called") {
                val requestXML = TREKK_V1_REQUEST_1_XML.readFromResource()
                val fromDate = LocalDate.now()
                val tomDate = LocalDate.now().plusMonths(1)
                val trekk = behandleTrekkvedtakService.behandleTrekkvedtak(requestXML, fromDate, tomDate, true)

                then("it should return a TrekkResponse") {
                    trekk.trekkResponse.size shouldBe 2
                    trekk.trekkResponse.forEach { it.beslutning shouldBe Beslutning.OS }

                    verify { arenaClientService.finnYtelseVedtakListe(any()) }
                    verify { producer.send(any(), any(), any()) }
                }
            }
        }

        given("skal returnere besluttning ingen Vedtak fra OS og ingen Vedtak fra Arena") {
            val ytelseVedtakListeResponse = YTELSEVEDTAK_RESPONSE_XML.readFromResource().unmarshalFinnYtelseVedtakListeResponse()
            every { arenaClientService.finnYtelseVedtakListe(any()) } returns ytelseVedtakListeResponse
            every { producer.send(any(), any(), any()) } returns Unit

            When("behandleTrekkvedtak is called") {
                val requestXML = TREKK_V1_REQUEST_2_XML.readFromResource()
                val fromDate = LocalDate.now()
                val tomDate = LocalDate.now().plusMonths(1)
                val trekk = behandleTrekkvedtakService.behandleTrekkvedtak(requestXML, fromDate, tomDate, true)

                then("it should return a TrekkResponse") {
                    trekk.trekkResponse.size shouldBe 2
                    trekk.trekkResponse.forEach { it.beslutning shouldBe Beslutning.INGEN }

                    verify { arenaClientService.finnYtelseVedtakListe(any()) }
                    verify { producer.send(any(), any(), any()) }
                }
            }
        }

        given("skal ikke sendes svar til MQ") {
            val ytelseVedtakListeResponse = YTELSEVEDTAK_RESPONSE_XML.readFromResource().unmarshalFinnYtelseVedtakListeResponse()
            every { arenaClientService.finnYtelseVedtakListe(any()) } returns ytelseVedtakListeResponse

            When("behandleTrekkvedtak is called") {
                val requestXML = TREKK_V1_REQUEST_2_XML.readFromResource()
                val fromDate = LocalDate.now()
                val tomDate = LocalDate.now().plusMonths(1)
                val trekk = behandleTrekkvedtakService.behandleTrekkvedtak(requestXML, fromDate, tomDate, false)

                then("it should return a TrekkResponse") {
                    trekk.trekkResponse.size shouldBe 2
                    trekk.trekkResponse.forEach { it.beslutning shouldBe Beslutning.INGEN }

                    verify { arenaClientService.finnYtelseVedtakListe(any()) }
                    verify(exactly = 0) { producer.send(any(), any(), any()) }
                }
            }
        }
    })
