package no.nav.sokos.trekk.api

import kotlinx.serialization.Serializable

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import mu.KotlinLogging

import no.nav.sokos.trekk.arenamock.ArenaMockService
import no.nav.sokos.trekk.config.PropertiesConfig

private val logger = KotlinLogging.logger {}

fun Route.arenaMockApi() {
    route("/api/arenamock") {
        get("/data") {
            logger.info { "Mottar mock data:" }
            call.respond(
                DataResponse(
                    xml = ArenaMockService.mockDataXml,
                    fasitEnv = PropertiesConfig.Configuration().naisAppName,
                ),
            )
        }

        post("/upload") {
            val xmlContent = call.receiveText()
            logger.info { "Mottar mock data: $xmlContent" }
            try {
                ArenaMockService.lagreArenaMockData(xmlContent)
                call.respond(HttpStatusCode.OK, "OK")
            } catch (e: Exception) {
                logger.error(e) { "Feil ved parsing" }
                call.respond(HttpStatusCode.BadRequest, e.localizedMessage)
            }
        }
    }
}

@Serializable
data class DataResponse(val xml: String?, val fasitEnv: String)
