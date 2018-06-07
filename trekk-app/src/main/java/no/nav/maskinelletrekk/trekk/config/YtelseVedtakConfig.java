package no.nav.maskinelletrekk.trekk.config;

import no.nav.maskinelletrekk.trekk.sikkerhet.STSClientConfig;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.YtelseVedtakV1;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;
import java.util.HashMap;

@Configuration
public class YtelseVedtakConfig {

    private static final String WSDL_URL = "/wsdl/no/nav/tjeneste/virksomhet/ytelseVedtak/v1/YtelseVedtakV1.wsdl";
    private static final String TARGET_NAMESPACE = "http://nav.no/tjeneste/virksomhet/ytelseVedtak/v1";
    private QName endpointName = new QName(TARGET_NAMESPACE, "YtelseVedtak_v1Port");
    private QName serviceName = new QName(TARGET_NAMESPACE, "YtelseVedtak_v1");

    @Value("${VIRKSOMHET_YTELSEVEDTAK_V1_ENDPOINTURL}")
    public String ytelseVedtakEndpoint;

    @Value("${SRVTREKK_USERNAME}")
    private String username;

    @Value("${SRVTREKK_PASSWORD}")
    private String password;

    @Value("${SECURITYTOKENSERVICE_URL}")
    private String location;

    @Bean
    public STSClientConfig stsClientConfig() {
        return new STSClientConfig(location, username, password);
    }

    @Bean
    public YtelseVedtakV1 ytelseVedtakService(STSClientConfig stsClientConfig) {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(WSDL_URL);
        factoryBean.setProperties(new HashMap<>());
        factoryBean.setServiceName(serviceName);
        factoryBean.setEndpointName(endpointName);
        factoryBean.setServiceClass(YtelseVedtakV1.class);
        factoryBean.setAddress(ytelseVedtakEndpoint);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
        YtelseVedtakV1 port = factoryBean.create(YtelseVedtakV1.class);
        return stsClientConfig.configureRequestSamlToken(port);
    }

}
