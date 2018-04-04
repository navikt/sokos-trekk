package no.nav.maskinelletrekk;

import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Service;

@Service
public class TrekkRoute extends SpringRouteBuilder {

    @Override
    public void configure() throws Exception {
        from("jms:TREKK_INN?transacted=false&cacheLevelName=CACHE_CONNECTION")
                .to("log:?level=INFO&showBody=true");
    }
}
