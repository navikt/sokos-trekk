package no.nav.maskinelletrekk.trekk.config;

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.YtelseVedtakV1;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.YtelseVedtakV1_Service;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.xml.ws.BindingProvider;
import java.time.Clock;
import java.util.Map;

@EnableJms
@EnableTransactionManagement
@EnableCaching
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector
@EnableConfigurationProperties({ChannelAlias.class, GatewayAlias.class})
@Configuration
public class TrekkConfig {

//    @Value("${}")
    private String endpoint = "http://localhost:8081/mockYtelseVedtakV1";

    @Bean
    public YtelseVedtakV1 ytelseVedtakService() {
        YtelseVedtakV1 ytelseVedtakV1 = new YtelseVedtakV1_Service().getYtelseVedtakV1Port();

        BindingProvider bindingProvider = (BindingProvider) ytelseVedtakV1;
        Map<String, Object> context = bindingProvider.getRequestContext();
        context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
        return ytelseVedtakV1;
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
