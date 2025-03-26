package no.nav.sokos.trekk.api

import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

import io.ktor.http.ContentType
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import mu.KotlinLogging

import no.nav.sokos.trekk.service.BehandleTrekkvedtakService
import no.nav.sokos.trekk.util.JaxbUtils

private val logger = KotlinLogging.logger {}

fun Route.trekkApi(behandleTrekkvedtakService: BehandleTrekkvedtakService = BehandleTrekkvedtakService()) {
    route("/api/trekk") {
        post("/behandle") {
            logger.info { "Behandle trekk data." }

            val xmlContent = call.receiveText()
            val trekk =
                behandleTrekkvedtakService.behandleTrekkvedtak(
                    xmlContent = xmlContent,
                    fromDate = LocalDate.now(),
                    toDate = LocalDate.now().plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()),
                    replyToQueue = false,
                )

            call.respondText(JaxbUtils.marshalTrekk(trekk), contentType = ContentType.Application.Xml)
        }
    }
}
