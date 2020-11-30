package no.nav.maskinelletrekk.trekk.behandletrekkvedtak;

import io.micrometer.core.instrument.Metrics;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;
import static no.nav.maskinelletrekk.trekk.config.Metrikker.AGGREGERT_MELDING_FRA_OS_COUNTER;
import static no.nav.maskinelletrekk.trekk.config.Metrikker.MELDING_TIL_OS_COUNTER;
import static no.nav.maskinelletrekk.trekk.config.Metrikker.MELDING_TIL_BOQ_COUNTER;
import static no.nav.maskinelletrekk.trekk.config.Metrikker.TAG_EXCEPTION_NAME;
import static no.nav.maskinelletrekk.trekk.config.Metrikker.TAG_LABEL_QUEUE;
import static org.apache.camel.LoggingLevel.INFO;

@Service
public class TrekkRoute extends RouteBuilder {

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
                .process(exchange -> {
                    Throwable throwable = exchange.getProperty(exchange.EXCEPTION_CAUGHT, Throwable.class);
                    Metrics.counter(MELDING_TIL_BOQ_COUNTER, TAG_EXCEPTION_NAME, throwable.getClass().getSimpleName())
                            .increment();
                })
                .to("ref:trekkInnBoq");

        from(BEHANDLE_TREKK_ROUTE)
                .routeId(BEHANDLE_TREKK_ROUTE_ID)
                .process(exchange -> Metrics.counter(AGGREGERT_MELDING_FRA_OS_COUNTER).increment())
                .setHeader(TYPE_KJORING, simple("${body.typeKjoring}"))
                .bean(behandleTrekkvedtak)
                .marshal(TREKK_FORMAT)
                .choice()
                    .when(header(TYPE_KJORING)
                            .in(PERIODISK_KONTROLL, RETURMELDING_TIL_TREKKINNMELDER))
                        .log(INFO, LOGGER, "Legger melding på BATCH_REPLY-kø: ${body}")
                        .process(exchange -> Metrics.counter(MELDING_TIL_OS_COUNTER, TAG_LABEL_QUEUE, "BATCH_REPLY")
                                .increment())
                        .to(TREKK_REPLY_BATCH_QUEUE)
                    .otherwise()
                        .log(INFO, LOGGER, "Legger melding på REPLY-kø: ${body}")
                        .process(exchange -> Metrics.counter(MELDING_TIL_OS_COUNTER, TAG_LABEL_QUEUE, "REPLY")
                                .increment())
                        .to(TREKK_REPLY_QUEUE)
                .endChoice();
    }

}
