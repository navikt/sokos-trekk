package no.nav.maskinelletrekk;

import no.nav.maskinelletrekk.trekk.v1.ObjectFactory;
import no.nav.maskinelletrekk.trekk.v1.Trekk;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.TrekkResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrekkBuilder {

    public List<TrekkRequest> trekkRequests;
    public List<TrekkResponse> trekkResponse;

    public TrekkBuilder() {
    }

    public static TrekkBuilder create() {
        return new TrekkBuilder();
    }

    public TrekkBuilder addTrekkRequest(TrekkRequest... requests) {
        if (trekkRequests == null) {
            trekkRequests = new ArrayList<>();
        }
        trekkRequests.addAll(Arrays.asList(requests));
        return this;
    }

    public Trekk build() {
        ObjectFactory factory = new ObjectFactory();
        Trekk trekk = factory.createTrekk();
        if (trekkRequests != null) {
            trekk.getTrekkRequest().addAll(trekkRequests);
        }
        if (trekkResponse != null) {
            trekk.getTrekkResponse().addAll(trekkResponse);
        }
        return trekk;
    }

}
