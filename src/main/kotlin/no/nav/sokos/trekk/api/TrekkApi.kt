package no.nav.sokos.trekk.api

import java.time.LocalDate

import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import mu.KotlinLogging

import no.nav.sokos.trekk.api.model.TrekkResponse
import no.nav.sokos.trekk.service.BehandleTrekkvedtakService

private val logger = KotlinLogging.logger {}

fun Route.trekkApi(behandleTrekkvedtakService: BehandleTrekkvedtakService = BehandleTrekkvedtakService()) {
    route("/api/trekk") {
        post("/behandle") {
            logger.info { "Behandle trekk data" }

            val xmlContent = call.receiveText()
            val trekkResponse = TrekkResponse(behandleTrekkvedtakService.behandleTrekkvedtak(xmlContent, LocalDate.now(), false).trekkResponse)
            call.respond(trekkResponse)
        }
    }
}
