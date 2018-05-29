package no.nav.maskinelletrekk.trekk.config;

import no.nav.maskinelletrekk.trekk.sikkerhet.StsConfigurationUtil;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.YtelseVedtakV1;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class YtelseVedtakConfig {

    private static final String WSDL_URL = "/wsdl/no/nav/tjeneste/virksomhet/ytelseVedtak/v1/YtelseVedtakV1.wsdl";
    private static final String TARGET_NAMESPACE = "http://nav.no/tjeneste/virksomhet/ytelseVedtak/v1";

    @Value("${VIRKSOMHET_YTELSEVEDTAK_V1_ENDPOINTURL}")
    public String ytelseVedtakEndpoint;

    @Value("${SRVTREKK_USERNAME}")
    private String username;

    @Value("${SRVTREKK_PASSWORD}")
    private String password;

    @Value("${SECURITYTOKENSERVICE_URL}")
    private String location;

    private QName endpointName = new QName(TARGET_NAMESPACE, "YtelseVedtak_v1Port");

    private QName serviceName = new QName(TARGET_NAMESPACE, "YtelseVedtak_v1");

    @Bean
    public YtelseVedtakV1 ytelseVedtakService() {
        Map<String, Object> properties = new HashMap<>();

        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(WSDL_URL);
        factoryBean.setProperties(properties);
        factoryBean.setServiceName(serviceName);
        factoryBean.setEndpointName(endpointName);
        factoryBean.setServiceClass(YtelseVedtakV1.class);
        factoryBean.setAddress(ytelseVedtakEndpoint);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
//        factoryBean.getOutInterceptors().add(new CallIdOutInterceptor());

        return StsConfigurationUtil.wrapWithSts(factoryBean.create(YtelseVedtakV1.class), username, password, location);

    }

}
