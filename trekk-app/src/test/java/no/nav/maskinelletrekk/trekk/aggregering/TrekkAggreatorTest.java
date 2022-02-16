package no.nav.maskinelletrekk.trekk.aggregering;

import no.nav.maskinelletrekk.trekk.v1.Trekk;
import no.nav.maskinelletrekk.trekk.v1.builder.TrekkBuilder;
import no.nav.maskinelletrekk.trekk.v1.builder.TrekkRequestBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TrekkAggreatorTest {

    private final TrekkAggreator trekkAggreator = new TrekkAggreator();

    private Exchange newExchange;
    private Exchange oldExchange;
    private Message oldMessage;
    private Message newMessage;

    @Before
    public void setUp() {
        oldExchange = mock(Exchange.class);
        newExchange = mock(Exchange.class);
        oldMessage = mock(Message.class);
        newMessage = mock(Message.class);
        when(this.oldExchange.getIn()).thenReturn(oldMessage);
        when(this.newExchange.getIn()).thenReturn(newMessage);
    }

    @Test
    public void skalReturnereNyExchangeVedFoersteKall() {
        Exchange aggregate = trekkAggreator.aggregate(null, newExchange);
        assertThat(aggregate, sameInstance(newExchange));
    }

    @Test
    public void skalAggregereMeldinger() {
        Trekk oldTrekk = TrekkBuilder.create().addTrekkRequest(
                TrekkRequestBuilder.create()
                        .offnr("b1234567")
                        .trekkvedtakId(12)
                        .build()
        ).build();
        when(oldMessage.getBody(Trekk.class)).thenReturn(oldTrekk);
        Trekk newTrekk = TrekkBuilder.create().addTrekkRequest(
                TrekkRequestBuilder.create()
                        .offnr("c1234568")
                        .trekkvedtakId(13)
                        .build()
        ).build();
        when(newMessage.getBody(Trekk.class)).thenReturn(newTrekk);

        Exchange aggregate = trekkAggreator.aggregate(oldExchange, newExchange);
        Trekk aggregatedBody = aggregate.getIn().getBody(Trekk.class);

        assertThat(aggregate, sameInstance(oldExchange));
        assertThat(aggregatedBody.getTrekkRequest().size(), equalTo(2));
        assertThat(aggregatedBody.getTrekkRequest().get(0), is(oldTrekk.getTrekkRequest().get(0)));
        assertThat(aggregatedBody.getTrekkRequest().get(1), is(newTrekk.getTrekkRequest().get(0)));
    }

}