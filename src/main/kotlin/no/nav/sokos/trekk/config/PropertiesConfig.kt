package no.nav.sokos.trekk.config

import java.io.File

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType

object PropertiesConfig {
    private val defaultProperties =
        ConfigurationMap(
            mapOf(
                "NAIS_APP_NAME" to "sokos-trekk",
                "NAIS_NAMESPACE" to "okonomi",
                "USE_AUTHENTICATION" to "true",
                "MQ_LISTENER_ENABLED" to "true",
            ),
        )

    private val localDevProperties =
        ConfigurationMap(
            mapOf(
                "APPLICATION_PROFILE" to Profile.LOCAL.toString(),
                "USE_AUTHENTICATION" to "false",
                "MQ_HOSTNAME" to "10.53.17.118",
                "MQ_PORT" to "1413",
                "MQ_QUEUE_MANAGER_NAME" to "MQLS01",
                "MQ_CHANNEL_NAME" to "Q1_TREKK",
                "MQ_TREKK_INN_QUEUE_NAME" to "QA.Q1_TREKK.TREKK_INN",
                "MQ_TREKK_INN_BOQ_QUEUE_NAME" to "Q1_TREKK.TREKK_INN_BOQ",
                "MQ_TREKK_REPLY_QUEUE_NAME" to "QA.Q1_231.OB04_TREKK_REPLY",
                "MQ_TREKK_REPLY_BATCH_QUEUE_NAME" to "QA.Q1_231.OB04_TREKK_REPLY_BATCH",
            ),
        )

    private val devProperties = ConfigurationMap(mapOf("APPLICATION_PROFILE" to Profile.DEV.toString()))
    private val prodProperties = ConfigurationMap(mapOf("APPLICATION_PROFILE" to Profile.PROD.toString()))

    private val config =
        when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
            "dev-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding devProperties overriding defaultProperties
            "prod-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding prodProperties overriding defaultProperties
            else ->
                ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding
                    ConfigurationProperties.fromOptionalFile(
                        File("defaults.properties"),
                    ) overriding localDevProperties overriding defaultProperties
        }

    operator fun get(key: String): String = config[Key(key, stringType)]

    fun getOrEmpty(key: String): String = config.getOrElse(Key(key, stringType), "")

    data class Configuration(
        val naisAppName: String = get("NAIS_APP_NAME"),
        val profile: Profile = Profile.valueOf(get("APPLICATION_PROFILE")),
        val useAuthentication: Boolean = getOrEmpty("USE_AUTHENTICATION").toBoolean(),
        val azureAdProperties: AzureAdProperties = AzureAdProperties(),
    )

    data class AzureAdProperties(
        val clientId: String = getOrEmpty("AZURE_APP_CLIENT_ID"),
        val wellKnownUrl: String = getOrEmpty("AZURE_APP_WELL_KNOWN_URL"),
        val tenantId: String = getOrEmpty("AZURE_APP_TENANT_ID"),
        val clientSecret: String = getOrEmpty("AZURE_APP_CLIENT_SECRET"),
    )

    data class MQProperties(
        val hostname: String = get("MQ_HOSTNAME"),
        val port: Int = get("MQ_PORT").toInt(),
        val mqQueueManagerName: String = get("MQ_QUEUE_MANAGER_NAME"),
        val mqChannelName: String = getOrEmpty("MQ_CHANNEL_NAME"),
        val userAuth: Boolean = true,
        val username: String = getOrEmpty("SRVTREKK_USERNAME"),
        val password: String = getOrEmpty("SRVTREKK_PASSWORD"),
        val trekkInnQueueName: String = getOrEmpty("MQ_TREKK_INN_QUEUE_NAME"),
        val trekkInnBoqQueueName: String = getOrEmpty("MQ_TREKK_INN_BOQ_QUEUE_NAME"),
        val trekkReplyQueueName: String = getOrEmpty("MQ_TREKK_REPLY_QUEUE_NAME"),
        val trekkReplyBatchQueueName: String = getOrEmpty("MQ_TREKK_REPLY_BATCH_QUEUE_NAME"),
        val mqListenerEnabled: Boolean = get("MQ_LISTENER_ENABLED").toBoolean(),
    )

    data class SoapProperties(
        val stsUrl: String = get("SECURITY_TOKENSERVICE_URL"),
        val ytelsevedtakV1EndpointUrl: String = get("VIRKSOMHET_YTELSEVEDTAK_V1_ENDPOINTURL"),
        val serviceUsername: String = getOrEmpty("SRVTREKK_USERNAME"),
        val servicePassword: String = getOrEmpty("SRVTREKK_PASSWORD"),
    )

    enum class Profile {
        LOCAL,
        DEV,
        PROD,
    }

    fun isLocal() = Configuration().profile == Profile.LOCAL

    fun isDev() = Configuration().profile == Profile.DEV
}
