package no.nav.maskinelletrekk.trekk.behandletrekkvedtak;

import no.nav.maskinelletrekk.trekk.config.Metrics;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;
import static no.nav.maskinelletrekk.trekk.config.Metrics.aggregerteMeldingerFraOSCounter;
import static no.nav.maskinelletrekk.trekk.config.Metrics.meldingerTilOSCounter;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;

@Service
public class TrekkRoute extends SpringRouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrekkRoute.class);

    private static final String TREKK_REPLY_QUEUE = "ref:trekkReply";
    private static final String TREKK_REPLY_BATCH_QUEUE = "ref:trekkReplyBatch";

    public static final String BEHANDLE_TREKK_ROUTE = "direct:behandleTrekk";
    private static final String BEHANDLE_TREKK_ROUTE_ID = "behandleTrekk";

    private static final String TYPE_KJORING = "typeKjoring";
    private static final String PERIODISK_KONTROLL = "PERI";
    private static final String RETURMELDING_TIL_TREKKINNMELDER = "REME";

    private static final DataFormat TREKK_FORMAT = new JaxbDataFormat("no.nav.maskinelletrekk.trekk.v1");

    private final BehandleTrekkvedtakBean behandleTrekkvedtak;

    @Autowired
    public TrekkRoute(BehandleTrekkvedtakBean behandleTrekkvedtak) {
        this.behandleTrekkvedtak = requireNonNull(behandleTrekkvedtak, "behandleTrekkvedtak must not be null");
    }

    @Override
    public void configure() {

        onException(Throwable.class)
                .handled(true)
                .useOriginalMessage()
                .marshal(TREKK_FORMAT)
                .logStackTrace(true)
                .log(ERROR, LOGGER, "Legger melding på backout-queue")
                .process(x -> Metrics.boqCounter.labels(x.getException().getClass().getCanonicalName()).inc())
                .to("ref:trekkInnBoq");

        from(BEHANDLE_TREKK_ROUTE)
                .routeId(BEHANDLE_TREKK_ROUTE_ID)
                .process(exchange -> aggregerteMeldingerFraOSCounter.inc())
                .setHeader(TYPE_KJORING, simple("${body.typeKjoring}"))
                .bean(behandleTrekkvedtak)
                .marshal(TREKK_FORMAT)
                .choice()
                    .when(header(TYPE_KJORING)
                            .in(PERIODISK_KONTROLL, RETURMELDING_TIL_TREKKINNMELDER))
                        .process(exchange -> meldingerTilOSCounter.labels("BATCH_REPLY").inc())
                        .to(TREKK_REPLY_BATCH_QUEUE)
                    .otherwise()
                        .process(exchange -> meldingerTilOSCounter.labels("REPLY").inc())
                        .to(TREKK_REPLY_QUEUE)
                .endChoice();
    }

}
