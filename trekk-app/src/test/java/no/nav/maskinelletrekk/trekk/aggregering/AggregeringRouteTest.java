package no.nav.maskinelletrekk.trekk.aggregering;

import no.nav.maskinelletrekk.trekk.helper.XmlHelper;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static no.nav.maskinelletrekk.trekk.aggregering.AggregeringRoute.AGGREGERING_ROUTE_ID;

@RunWith(MockitoJUnitRunner.class)
public class AggregeringRouteTest extends CamelTestSupport {

    private static final String DIRECT_AGGREGER_BESTILLING = "direct:aggreger_bestilling";

    @Mock
    @Produce(uri = DIRECT_AGGREGER_BESTILLING)
    private ProducerTemplate aggregeringMock;

    @Spy
    private TrekkAggreator trekkAggregator;

    @InjectMocks
    private AggregeringRoute aggregeringRoute;


    @Override
    protected void doPostSetup() throws Exception {
        context.getRouteDefinition(AGGREGERING_ROUTE_ID).adviceWith(context, aggregeringAdvice());
        context.start();
    }

    private AdviceWithRouteBuilder aggregeringAdvice() {
        return new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                replaceFromWith(DIRECT_AGGREGER_BESTILLING);
            }
        };
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
}