package no.nav.maskinelletrekk.trekk.behandletrekkvedtak;

import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import static no.nav.maskinelletrekk.trekk.config.PrometheusLabels.PROCESS_TREKK;
import static no.nav.maskinelletrekk.trekk.config.PrometheusMetrics.aggregerteMeldingerFraOSCounter;
import static no.nav.maskinelletrekk.trekk.config.PrometheusMetrics.meldingerTilOSCounter;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;

@Service
public class TrekkRoute extends SpringRouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrekkRoute.class);

    private static final String TREKK_REPLY_QUEUE = "ref:trekkReply";
    private static final String TREKK_REPLY_BTC_QUEUE = "ref:trekkReplyBtc";

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

        onException(Throwable.class)
                .handled(true)
                .useOriginalMessage()
                .marshal(TREKK_FORMAT)
                .log(ERROR, LOGGER, "Legger melding på backout-queue ${body}")
                .to("ref:trekkInnBoq");

        from(BEHANDLE_TREKK_ROUTE)
                .routeId(BEHANDLE_TREKK_ROUTE_ID)
                .process(exchange -> aggregerteMeldingerFraOSCounter.labels(PROCESS_TREKK, "Mottatt aggregert melding fra OS").inc())
                .bean(behandleTrekkvedtak)
                .marshal(TREKK_FORMAT)
                .log(INFO, LOGGER, "Legger melding på reply-kø: ${body}")
                .process(exchange -> meldingerTilOSCounter.labels(PROCESS_TREKK, "Sender melding til OS").inc())
                .to(TREKK_REPLY_QUEUE);
    }

}
