package no.nav.maskinelletrekk;

import no.nav.maskinelletrekk.trekk.v1.ObjectFactory;
import no.nav.maskinelletrekk.trekk.v1.Oppdragsvedtak;
import no.nav.maskinelletrekk.trekk.v1.Periode;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class TrekkRequestBuilder {

    private String bruker;
    private Periode periode;
    private int trekkvedtakId;
    private List<Oppdragsvedtak> oppdragsvedtakList;

    private ObjectFactory of = new ObjectFactory();

    private TrekkRequestBuilder() {
    }

    public static TrekkRequestBuilder create() {
        return new TrekkRequestBuilder();
    }

	public TrekkRequestBuilder bruker(String bruker) {this.bruker = bruker; return this;}

	public TrekkRequestBuilder periode(Periode periode) {this.periode = periode; return this;}

	public TrekkRequestBuilder periode(LocalDate fom, LocalDate tom) {
        Periode periode = of.createPeriode();
        periode.setTom(tom);
        periode.setFom(fom);
        this.periode = periode;
        return this;
    }

    public TrekkRequestBuilder trekkvedtakId(int trekkvedtakId) {this.trekkvedtakId = trekkvedtakId; return this;}

    public TrekkRequestBuilder oppdragsvedtak(List<Oppdragsvedtak> oppdragsvedtakList) {this.oppdragsvedtakList = oppdragsvedtakList; return this;}

    public TrekkRequestBuilder addOppdragsvedtak(BigDecimal value, LocalDate fom, LocalDate tom) {
        if (oppdragsvedtakList == null) {
            oppdragsvedtakList = new ArrayList<>();
        }
        Oppdragsvedtak oppdragsvedtak = of.createOppdragsvedtak();
        oppdragsvedtak.setSats(value);
        Periode per = of.createPeriode();
        per.setFom(fom);
        per.setTom(tom);
        oppdragsvedtak.setPeriode(per);
        oppdragsvedtakList.add(oppdragsvedtak);
        return this;
    }

    public TrekkRequest build() {
        TrekkRequest trekkRequest = new ObjectFactory().createTrekkRequest();
        trekkRequest.setBruker(bruker);
        trekkRequest.setPeriode(periode);
        trekkRequest.setTrekkvedtakId(trekkvedtakId);
        trekkRequest.getOppdragsvedtak().addAll(oppdragsvedtakList);
        return trekkRequest;
    }
}
