package no.nav.maskinelletrekk.trekk.v1.builder;

import no.nav.maskinelletrekk.trekk.v1.ObjectFactory;
import no.nav.maskinelletrekk.trekk.v1.Trekk;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.TrekkResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrekkBuilder {

    private List<TrekkRequest> trekkRequestList;
    private List<TrekkResponse> trekkResponseList;

    private TrekkBuilder() {
    }

    public static TrekkBuilder create() {
        return new TrekkBuilder();
    }

    public TrekkBuilder addTrekkRequest(TrekkRequest... requests) {
        if (trekkRequestList == null) {
            trekkRequestList = new ArrayList<>();
        }
        trekkRequestList.addAll(Arrays.asList(requests));
        return this;
    }

    public TrekkBuilder addTrekkResponse(TrekkResponse... responses) {
        if (trekkResponseList == null) {
            trekkResponseList = new ArrayList<>();
        }
        trekkResponseList.addAll(Arrays.asList(responses));
        return this;
    }

    public Trekk build() {
        ObjectFactory factory = new ObjectFactory();
        Trekk trekk = factory.createTrekk();
        if (trekkRequestList != null && trekkResponseList == null) {
            trekk.getTrekkRequest().addAll(trekkRequestList);
        } else if (trekkResponseList != null) {
            trekk.getTrekkResponse().addAll(trekkResponseList);
        }
        return trekk;
    }

}
