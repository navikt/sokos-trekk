package no.nav.maskinelletrekk.trekk.behandletrekkvedtak;

import io.micrometer.core.instrument.Metrics;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;
import static no.nav.maskinelletrekk.trekk.config.Metrikker.AGGREGERTE_MELDINGER_FRA_OS;
import static no.nav.maskinelletrekk.trekk.config.Metrikker.MELDINGER_TIL_OS;
import static no.nav.maskinelletrekk.trekk.config.Metrikker.MESSAGES_ON_BOQ;
import static no.nav.maskinelletrekk.trekk.config.Metrikker.TAG_EXCEPTION_NAME;
import static no.nav.maskinelletrekk.trekk.config.Metrikker.TAG_LABEL_QUEUE;

@Service
public class TrekkRoute extends RouteBuilder {

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
                    Metrics.counter(MESSAGES_ON_BOQ, TAG_EXCEPTION_NAME, throwable.getClass().getSimpleName())
                            .increment();
                })
                .to("ref:trekkInnBoq");

        from(BEHANDLE_TREKK_ROUTE)
                .routeId(BEHANDLE_TREKK_ROUTE_ID)
                .process(exchange -> Metrics.counter(AGGREGERTE_MELDINGER_FRA_OS).increment())
                .setHeader(TYPE_KJORING, simple("${body.typeKjoring}"))
                .bean(behandleTrekkvedtak)
                .marshal(TREKK_FORMAT)
                .choice()
                    .when(header(TYPE_KJORING)
                            .in(PERIODISK_KONTROLL, RETURMELDING_TIL_TREKKINNMELDER))
                        .process(exchange -> Metrics.counter(MELDINGER_TIL_OS, TAG_LABEL_QUEUE, "BATCH_REPLY").increment())
                        .to(TREKK_REPLY_BATCH_QUEUE)
                    .otherwise()
                        .process(exchange -> Metrics.counter(MELDINGER_TIL_OS, TAG_LABEL_QUEUE, "REPLY").increment())
                        .to(TREKK_REPLY_QUEUE)
                .endChoice();
    }

}
