package no.nav.maskinelletrekk.trekk.config;

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
@Configuration
public class TrekkConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

}
