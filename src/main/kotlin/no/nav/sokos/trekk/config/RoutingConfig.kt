package no.nav.sokos.trekk.config

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing

import no.nav.sokos.trekk.api.arenaMockApi
import no.nav.sokos.trekk.api.trekkApi

fun Application.routingConfig(
    useAuthentication: Boolean,
    applicationState: ApplicationState,
) {
    routing {
        internalNaisRoutes(applicationState)
        if (PropertiesConfig.isDev() || PropertiesConfig.isLocal()) {
            authenticate(useAuthentication, AUTHENTICATION_NAME) {
                trekkApi()
            }
            arenaMockApi()
        }
    }
}

fun Route.authenticate(
    useAuthentication: Boolean,
    authenticationProviderId: String? = null,
    block: Route.() -> Unit,
) {
    if (useAuthentication) authenticate(authenticationProviderId) { block() } else block()
}
