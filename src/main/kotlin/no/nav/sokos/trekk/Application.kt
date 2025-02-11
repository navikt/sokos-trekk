package no.nav.sokos.trekk

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

import no.nav.sokos.trekk.config.ApplicationState
import no.nav.sokos.trekk.config.PropertiesConfig
import no.nav.sokos.trekk.config.applicationLifecycleConfig
import no.nav.sokos.trekk.config.commonConfig
import no.nav.sokos.trekk.config.routingConfig
import no.nav.sokos.trekk.config.securityConfig
import no.nav.sokos.trekk.mq.JmsListenerService

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(true)
}

private fun Application.module() {
    val useAuthentication = PropertiesConfig.Configuration().useAuthentication
    val applicationState = ApplicationState()
    commonConfig()
    applicationLifecycleConfig(applicationState)
    securityConfig(useAuthentication)
    if (PropertiesConfig.isDev() || PropertiesConfig.isLocal()) {
        routingConfig(useAuthentication, applicationState)
    }

    if (PropertiesConfig.MQProperties().mqListenerEnabled) {
        JmsListenerService().start()
    }
}
