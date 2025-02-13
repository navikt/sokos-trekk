package no.nav.sokos.trekk.config

import no.nav.common.cxf.StsConfig

class STSTokenProviderConfig {
    fun stsConfig(): StsConfig =
        StsConfig.builder()
            .url(PropertiesConfig.SoapProperties().stsUrl)
            .username(PropertiesConfig.ServiceUserProperties().serviceUsername)
            .password(PropertiesConfig.ServiceUserProperties().servicePassword)
            .build()
}
