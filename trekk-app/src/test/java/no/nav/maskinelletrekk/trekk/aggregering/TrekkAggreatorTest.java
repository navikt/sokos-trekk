package no.nav.maskinelletrekk.trekk.aggregering;

import no.nav.maskinelletrekk.trekk.v1.Trekk;
import no.nav.maskinelletrekk.trekk.v1.builder.TrekkBuilder;
import no.nav.maskinelletrekk.trekk.v1.builder.TrekkRequestBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TrekkAggreatorTest {

    private TrekkAggreator trekkAggreator = new TrekkAggreator();

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
                        .bruker("b1234567")
                        .antallDager(60)
                        .trekkvedtakId(12)
                        .addOppdragsvedtak(new BigDecimal("123.10"), LocalDate.now(), LocalDate.now())
                        .build()
        ).build();
        when(oldMessage.getBody(Trekk.class)).thenReturn(oldTrekk);
        Trekk newTrekk = TrekkBuilder.create().addTrekkRequest(
                TrekkRequestBuilder.create()
                        .bruker("c1234568")
                        .antallDager(60)
                        .trekkvedtakId(13)
                        .addOppdragsvedtak(new BigDecimal("223.10"), LocalDate.now(), LocalDate.now())
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