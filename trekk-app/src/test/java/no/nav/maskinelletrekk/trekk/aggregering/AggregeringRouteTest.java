package no.nav.maskinelletrekk.trekk.aggregering;

import no.nav.maskinelletrekk.trekk.helper.XmlHelper;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import static no.nav.maskinelletrekk.trekk.aggregering.AggregeringRoute.AGGREGERING_ROUTE_ID;

public class AggregeringRouteTest extends CamelTestSupport {

    private static final String DIRECT_AGGREGER_BESTILLING = "direct:aggreger_bestilling";

    @Produce(DIRECT_AGGREGER_BESTILLING)
    private ProducerTemplate aggregeringMock;

    private final AggregeringRoute aggregeringRoute = new AggregeringRoute(new TrekkAggreator());

    @Override
    protected void doPostSetup() throws Exception {
        AdviceWithRouteBuilder.adviceWith(context, AGGREGERING_ROUTE_ID,
                routeBuilder -> routeBuilder.replaceFromWith(DIRECT_AGGREGER_BESTILLING));
        context.start();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return aggregeringRoute;
    }

    @Override
    public boolean isUseAdviceWith() {
        return true;
    }

    @Test
    public void skalAggregereMeldinger() throws Exception {
        String body = XmlHelper.getRequestFromXmlAsString("trekkrequest_testcase1.xml");
        aggregeringMock.sendBody(body);
    }

    @Test(expected = CamelExecutionException.class)
    public void skalKasteExceptionVedUgyldigMelding() {
        aggregeringMock.sendBody("invalid");
    }
}