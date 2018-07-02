package no.nav.maskinelletrekk.trekk.config;

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Clock;

@EnableJms
@EnableTransactionManagement
@EnableCaching
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector
@EnableConfigurationProperties({ChannelAlias.class, GatewayAlias.class})
@Configuration
public class TrekkConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

}
