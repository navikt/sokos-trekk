package no.nav.sokos.trekk.config

import org.apache.cxf.Bus
import org.apache.cxf.BusFactory
import org.apache.cxf.binding.soap.Soap12
import org.apache.cxf.binding.soap.SoapMessage
import org.apache.cxf.endpoint.Client
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.rt.security.SecurityConstants
import org.apache.cxf.ws.policy.PolicyBuilder
import org.apache.cxf.ws.policy.PolicyBuilderImpl
import org.apache.cxf.ws.policy.PolicyEngine
import org.apache.cxf.ws.policy.attachment.reference.RemoteReferenceResolver
import org.apache.cxf.ws.security.trust.STSClient
import org.apache.neethi.Policy
import ulid.ULID

import no.nav.sokos.trekk.soap.CallIdInterceptor

private const val STS_CLIENT_AUTHENTICATION_POLICY = "classpath:policy/untPolicy.xml"
private const val STS_SAML_POLICY = "classpath:policy/requestSamlPolicy.xml"

object STSClientConfig {
    fun stsClient(
        stsUrl: String,
        credentials: Pair<String, String>,
        callIdGenerator: () -> String = { ULID.randomULID() },
    ): STSClient {
        val bus: Bus = BusFactory.getDefaultBus(true)

        return STSClient(bus).apply {
            isEnableAppliesTo = false
            isAllowRenewing = false
            location = stsUrl
            outInterceptors = listOf(CallIdInterceptor(callIdGenerator))
            properties =
                mapOf(
                    SecurityConstants.USERNAME to credentials.first,
                    SecurityConstants.PASSWORD to credentials.second,
                )
            setPolicy(bus.resolvePolicy(STS_CLIENT_AUTHENTICATION_POLICY))
        }
    }

    fun <T> STSClient.configureFor(servicePort: T) {
        configureFor(servicePort, STS_SAML_POLICY)
    }

    fun <T> STSClient.configureFor(
        servicePort: T,
        policyUri: String,
    ) {
        val client = ClientProxy.getClient(servicePort)
        client.configureSTS(this, policyUri)
    }

    private fun Client.configureSTS(
        stsClient: STSClient,
        policyUri: String = STS_SAML_POLICY,
    ) {
        requestContext[SecurityConstants.STS_CLIENT] = stsClient
        requestContext[SecurityConstants.CACHE_ISSUED_TOKEN_IN_ENDPOINT] = true
        setClientEndpointPolicy(bus.resolvePolicy(policyUri))
    }

    private fun Bus.resolvePolicy(policyUri: String): Policy {
        val registry = getExtension(PolicyEngine::class.java)?.registry
        val resolved = registry?.lookup(policyUri)

        if (getExtension(PolicyBuilder::class.java) == null) {
            setExtension(PolicyBuilderImpl(), PolicyBuilder::class.java)
        }

        val policyBuilder = getExtension(PolicyBuilder::class.java)
        val remoteReferenceResolver = RemoteReferenceResolver("", policyBuilder)

        return resolved ?: remoteReferenceResolver.resolveReference(policyUri)
    }

    private fun Client.setClientEndpointPolicy(policy: Policy) {
        val policyEngine: PolicyEngine = bus.getExtension(PolicyEngine::class.java)
        val message = SoapMessage(Soap12.getInstance())
        val endpointPolicy = policyEngine.getServerEndpointPolicy(endpoint.endpointInfo, null, message)
        val updatedPolicy = endpointPolicy.updatePolicy(policy, message)
        policyEngine.setClientEndpointPolicy(endpoint.endpointInfo, updatedPolicy)
    }
}
