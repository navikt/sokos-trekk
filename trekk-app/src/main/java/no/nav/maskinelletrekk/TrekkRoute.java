package no.nav.maskinelletrekk;

import no.nav.maskinelletrekk.trekk.v1.Trekk;
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

    private static final DataFormat trekkFormat = new JaxbDataFormat("no.nav.maskinelletrekk.trekk.v1");

    private BehandleTrekkvedtakBean kallArena;

    @Autowired
    public TrekkRoute(BehandleTrekkvedtakBean kallArena) {
        Assert.notNull(kallArena, "kallArena must not be null");
        this.kallArena = kallArena;
    }

    @Override
    public void configure() {

        errorHandler(defaultErrorHandler().useOriginalMessage());

        from("ref:trekkInn")
                .routeId("behandleTrekk")
                .convertBodyTo(String.class, "UTF-8")
                .log(INFO, LOGGER, "Melding lest fra kø (trekk_inn): ${body}")
                .to("direct:validateAndUnmarshal")
                .bean(kallArena)
                .marshal(trekkFormat)
                .to("ref:trekkReply");

        from("direct:validateAndUnmarshal")
                .routeId("ValidateAndUnmarshal")
                .to("validator:schema/trekk_v1.xsd")
                .unmarshal(trekkFormat)
                .validate(bodyAs(Trekk.class).isNotNull())
                .setBody(simple("${body.request}"));

    }
}
