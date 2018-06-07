package no.nav.maskinelletrekk.trekk.sikkerhet;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.ws.policy.EndpointPolicy;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.attachment.reference.ReferenceResolver;
import org.apache.cxf.ws.policy.attachment.reference.RemoteReferenceResolver;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.trust.STSClient;
import org.apache.neethi.Policy;

import java.util.ArrayList;
import java.util.HashMap;

import static java.util.Collections.singletonList;

public class STSClientConfig {

    private String location;
    private String username;
    private String password;

    private static final String STS_REQUEST_SAML_POLICY = "classpath:policy/requestSamlPolicyNoTransportBinding.xml";
    private static final String STS_CLIENT_AUTHENTICATION_POLICY = "classpath:policy/untPolicy.xml";

    public STSClientConfig(String location, String username, String password) {
        this.location = location;
        this.username = username;
        this.password = password;
    }

    public <T> T configureRequestSamlToken(T port) {
        Client client = ClientProxy.getClient(port);
        configureStsRequestSamlToken(client, true);
        return port;
    }

    private void configureStsRequestSamlToken(Client client, boolean cacheTokenInEndpoint) {
        STSClient stsClient = createCustomSTSClient(client.getBus());
        configureStsWithPolicyForClient(stsClient, client, STS_REQUEST_SAML_POLICY, cacheTokenInEndpoint);
    }


    private void configureStsWithPolicyForClient(STSClient stsClient, Client client, String policyReference, boolean cacheTokenInEndpoint) {

        configureSTSClient(stsClient, location, username, password);

        client.getRequestContext().put(SecurityConstants.STS_CLIENT, stsClient);
        client.getRequestContext().put(SecurityConstants.CACHE_ISSUED_TOKEN_IN_ENDPOINT, cacheTokenInEndpoint);
        setEndpointPolicyReference(client, policyReference);
    }

    private static STSClient createCustomSTSClient(Bus bus) {
        return new STSClientWSTrust13and14(bus);
    }

    private static STSClient configureSTSClient(STSClient stsClient, String location, String username, String password) {

        stsClient.setEnableAppliesTo(false);
        stsClient.setAllowRenewing(false);
        stsClient.setLocation(location);
        //For debugging
        stsClient.setFeatures(new ArrayList<Feature>(singletonList(new LoggingFeature())));

        HashMap<String, Object> properties = new HashMap<>();
        properties.put(SecurityConstants.USERNAME, username);
        properties.put(SecurityConstants.PASSWORD, password);

        stsClient.setProperties(properties);

        //used for the STS client to authenticate itself to the STS provider.
        stsClient.setPolicy(STS_CLIENT_AUTHENTICATION_POLICY);
        return stsClient;
    }

    private static void setEndpointPolicyReference(Client client, String uri) {
        Policy policy = resolvePolicyReference(client, uri);
        setClientEndpointPolicy(client, policy);
    }

    private static Policy resolvePolicyReference(Client client, String uri) {
        PolicyBuilder policyBuilder = client.getBus().getExtension(PolicyBuilder.class);
        ReferenceResolver resolver = new RemoteReferenceResolver("", policyBuilder);
        return resolver.resolveReference(uri);
    }

    private static void setClientEndpointPolicy(Client client, Policy policy) {
        Endpoint endpoint = client.getEndpoint();
        EndpointInfo endpointInfo = endpoint.getEndpointInfo();
        PolicyEngine policyEngine = client.getBus().getExtension(PolicyEngine.class);
        SoapMessage message = new SoapMessage(Soap12.getInstance());
        EndpointPolicy endpointPolicy = policyEngine.getClientEndpointPolicy(endpointInfo, null, message);
        policyEngine.setClientEndpointPolicy(endpointInfo, endpointPolicy.updatePolicy(policy, message));
    }

}