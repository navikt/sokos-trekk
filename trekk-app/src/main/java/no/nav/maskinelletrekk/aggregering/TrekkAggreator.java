package no.nav.maskinelletrekk.aggregering;

import no.nav.maskinelletrekk.trekk.v1.Trekk;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.springframework.stereotype.Component;

@Component
public class TrekkAggreator implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            return newExchange;
        }
        Trekk oldBody = oldExchange.getIn().getBody(Trekk.class);
        Trekk newBody = newExchange.getIn().getBody(Trekk.class);

        oldBody.getTrekkRequest()
                .addAll(newBody.getTrekkRequest());

        return oldExchange;
    }

}
