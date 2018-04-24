package no.nav.maskinelletrekk.behandletrekkvedtak;

import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import static org.apache.camel.LoggingLevel.INFO;

@Service
public class TrekkRoute extends SpringRouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrekkRoute.class);

    private static final String TREKK_REPLY_QUEUE = "ref:trekkReply";

    public static final String BEHANDLE_TREKK_ROUTE = "direct:behandleTrekk";
    private static final String BEHANDLE_TREKK_ROUTE_ID = "behandleTrekk";

    private static final DataFormat TREKK_FORMAT = new JaxbDataFormat("no.nav.maskinelletrekk.trekk.v1");

    private BehandleTrekkvedtakBean behandleTrekkvedtak;

    @Autowired
    public TrekkRoute(BehandleTrekkvedtakBean behandleTrekkvedtak) {
        Assert.notNull(behandleTrekkvedtak, "behandleTrekkvedtak must not be null");
        this.behandleTrekkvedtak = behandleTrekkvedtak;
    }

    @Override
    public void configure() {

        errorHandler(defaultErrorHandler().useOriginalMessage());

        from(BEHANDLE_TREKK_ROUTE)
                .routeId(BEHANDLE_TREKK_ROUTE_ID)
                .bean(behandleTrekkvedtak)
                .marshal(TREKK_FORMAT)
                .log(INFO, LOGGER, "Legger melding på reply-kø: ${body}")
                .to(TREKK_REPLY_QUEUE);
    }

}
