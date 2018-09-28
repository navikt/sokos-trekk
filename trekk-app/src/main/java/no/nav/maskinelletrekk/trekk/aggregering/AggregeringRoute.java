package no.nav.maskinelletrekk.trekk.aggregering;

import no.nav.maskinelletrekk.trekk.v1.Trekk;
import org.apache.camel.LoggingLevel;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.format.DateTimeParseException;

import static no.nav.maskinelletrekk.trekk.behandletrekkvedtak.TrekkRoute.BEHANDLE_TREKK_ROUTE;
import static no.nav.maskinelletrekk.trekk.config.PrometheusLabels.PROCESS_TREKK;
import static no.nav.maskinelletrekk.trekk.config.PrometheusMetrics.meldingerFraOSCounter;
import static org.apache.camel.LoggingLevel.ERROR;

@Service
public class AggregeringRoute extends SpringRouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregeringRoute.class);

    static final String AGGREGERING_ROUTE_ID = "aggreger_meldinger";
    private static final String VALIDATE_AND_UNMARSHAL_ROUTE = "direct:validateAndUnmarshal";
    private static final String VALIDATE_AND_UNMARSHAL_ROUTE_ID = "ValidateAndUnmarshal";
    private static final String TREKK_INN_QUEUE = "ref:trekkInn";
    private static final DataFormat TREKK_FORMAT = new JaxbDataFormat("no.nav.maskinelletrekk.trekk.v1");

    @Value("${trekk.aggregator.completionTimeout}")
    private int completionTimeout = 10000;

    @Value("${trekk.aggregator.completionSize}")
    private int completionSize = 30;

    private TrekkAggreator trekkAggregator;

    public AggregeringRoute(TrekkAggreator trekkAggregator) {
        Assert.notNull(trekkAggregator, "trekkAggregator must not be null");
        this.trekkAggregator = trekkAggregator;
    }

    @Override
    public void configure() {

        LOGGER.info("Aggregator parametere: completionTimeout: {} completionSize: {}", completionTimeout, completionSize);

        errorHandler(defaultErrorHandler().useOriginalMessage());
        onException(DateTimeParseException.class).log(ERROR, LOGGER, "Parsing av dato i request XML feilet");

        from(TREKK_INN_QUEUE)
                .routeId(AGGREGERING_ROUTE_ID)
                .log(LoggingLevel.INFO, LOGGER, "Mottatt melding fra OS ${body}")
                .to(VALIDATE_AND_UNMARSHAL_ROUTE)
                .process(exchange -> meldingerFraOSCounter.labels(PROCESS_TREKK, "Mottatt melding fra OS").inc())
                .aggregate(simple("${body.typeKjoring}"), trekkAggregator)
                .completionTimeout(completionTimeout)
                .completionSize(completionSize)
                .to(BEHANDLE_TREKK_ROUTE);

        from(VALIDATE_AND_UNMARSHAL_ROUTE)
                .routeId(VALIDATE_AND_UNMARSHAL_ROUTE_ID)
                .to("validator:schema/trekk_v1.xsd")
                .unmarshal(TREKK_FORMAT)
                .validate(bodyAs(Trekk.class).isNotNull());
    }

}
