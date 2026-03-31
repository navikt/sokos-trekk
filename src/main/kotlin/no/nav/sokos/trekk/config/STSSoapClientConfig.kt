package no.nav.sokos.trekk.config

import javax.xml.namespace.QName

import org.apache.cxf.Bus
import org.apache.cxf.binding.soap.Soap12
import org.apache.cxf.binding.soap.SoapMessage
import org.apache.cxf.bus.extension.ExtensionManagerBus
import org.apache.cxf.configuration.jsse.TLSClientParameters
import org.apache.cxf.endpoint.Client
import org.apache.cxf.ext.logging.LoggingFeature
import org.apache.cxf.ext.logging.event.LogMessageFormatter
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.frontend.ClientProxyFactoryBean
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.rt.security.SecurityConstants
import org.apache.cxf.transport.http.HTTPConduit
import org.apache.cxf.ws.addressing.WSAddressingFeature
import org.apache.cxf.ws.policy.PolicyBuilder
import org.apache.cxf.ws.policy.PolicyEngine
import org.apache.cxf.ws.policy.attachment.reference.RemoteReferenceResolver
import org.apache.cxf.ws.security.trust.STSClient
import org.apache.neethi.Policy
import ulid.ULID

import no.nav.sokos.trekk.soap.CallIdInterceptor
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.YtelseVedtakV1

const val NAMESPACE = "http://nav.no/tjeneste/virksomhet/ytelseVedtak/v1"

private const val STS_CLIENT_AUTHENTICATION_POLICY = "classpath:policy/untPolicy.xml"
private const val STS_SAML_POLICY = "classpath:policy/requestSamlPolicy.xml"
private const val WSDL_URL = "wsdl/no/nav/tjeneste/virksomhet/ytelseVedtak/v1/YtelseVedtakV1.wsdl"
private const val YTELSE_VEDTAK_SERVICE_NAME = "YtelseVedtak_v1"
private const val YTELSE_VEDTAK_ENDPOINT_NAME = "YtelseVedtak_v1Port"

private val logger = mu.KotlinLogging.logger { }

data class ServiceUserConfig(
    val username: String,
    val password: String,
)

internal inline fun <reified T> ClientProxyFactoryBean.wrapInStsClient(
    stsSoapUrl: String,
    serviceUser: ServiceUserConfig,
    disableCNCheck: Boolean,
): T =
    this.create(T::class.java).apply {
        val bus: Bus = ExtensionManagerBus()
        val sts =
            STSClient(bus).apply {
                isEnableAppliesTo = false
                isAllowRenewing = false

                location = stsSoapUrl
                properties =
                    mapOf(
                        SecurityConstants.USERNAME to serviceUser.username,
                        SecurityConstants.PASSWORD to serviceUser.password,
                    )
                setPolicy(bus.resolvePolicy(STS_CLIENT_AUTHENTICATION_POLICY))
            }
        ClientProxy.getClient(this).apply {
            requestContext[SecurityConstants.STS_CLIENT] = sts
            requestContext[SecurityConstants.CACHE_ISSUED_TOKEN_IN_ENDPOINT] = true
            setClientEndpointPolicy(bus.resolvePolicy(STS_SAML_POLICY))
            if (disableCNCheck) {
                val conduit = conduit as HTTPConduit
                conduit.tlsClientParameters =
                    TLSClientParameters().apply {
                        isDisableCNCheck = true
                    }
            }
        }
    }

internal fun Bus.resolvePolicy(policyUri: String): Policy {
    val registry = getExtension(PolicyEngine::class.java).registry
    val resolved = registry.lookup(policyUri)

    val policyBuilder = getExtension(PolicyBuilder::class.java)
    val referenceResolver = RemoteReferenceResolver("", policyBuilder)

    return resolved ?: referenceResolver.resolveReference(policyUri)
}

internal fun Client.setClientEndpointPolicy(policy: Policy) {
    val policyEngine: PolicyEngine = bus.getExtension(PolicyEngine::class.java)
    val message = SoapMessage(Soap12.getInstance())
    val endpointPolicy = policyEngine.getClientEndpointPolicy(endpoint.endpointInfo, null, message)
    policyEngine.setClientEndpointPolicy(endpoint.endpointInfo, endpointPolicy.updatePolicy(policy, message))
}

internal fun getYtelsesVedtakSoapClient(soapProperties: PropertiesConfig.SoapProperties): YtelseVedtakV1 =
    JaxWsProxyFactoryBean()
        .apply {
            address = soapProperties.ytelsevedtakV1EndpointUrl
            wsdlURL = WSDL_URL
            serviceName = QName(NAMESPACE, YTELSE_VEDTAK_SERVICE_NAME)
            endpointName = QName(NAMESPACE, YTELSE_VEDTAK_ENDPOINT_NAME)
            serviceClass = YtelseVedtakV1::class.java
            features = listOf(WSAddressingFeature(), loggingFeature)
            outInterceptors.add(CallIdInterceptor { ULID.randomULID() })
        }.wrapInStsClient(
            soapProperties.stsUrl,
            ServiceUserConfig(
                soapProperties.serviceUsername,
                soapProperties.servicePassword,
            ),
            soapProperties.disableCnCheck,
        )

private val loggingFeature =
    LoggingFeature()
        .apply {
            setVerbose(true)
            setPrettyLogging(true)
        }.also {
            it.setSender { event -> logger.debug { "SOAP melding -> ${LogMessageFormatter.format(event)}" } }
        }
