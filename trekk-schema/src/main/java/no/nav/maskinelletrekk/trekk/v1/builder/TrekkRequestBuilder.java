package no.nav.maskinelletrekk.trekk.v1.builder;

import no.nav.maskinelletrekk.trekk.v1.ObjectFactory;
import no.nav.maskinelletrekk.trekk.v1.Oppdragsvedtak;
import no.nav.maskinelletrekk.trekk.v1.Periode;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TrekkRequestBuilder {

    private String bruker;
    private int antallDager;
    private int trekkvedtakId;
    private List<Oppdragsvedtak> oppdragsvedtakList;

    private ObjectFactory of = new ObjectFactory();

    private TrekkRequestBuilder() {
    }

    public static TrekkRequestBuilder create() {
        return new TrekkRequestBuilder();
    }

    public TrekkRequestBuilder bruker(String bruker) {
        this.bruker = bruker;
        return this;
    }

    public TrekkRequestBuilder antallDager(int antallDager) {
        this.antallDager = antallDager;
        return this;
    }

    public TrekkRequestBuilder trekkvedtakId(int trekkvedtakId) {
        this.trekkvedtakId = trekkvedtakId;
        return this;
    }

    public TrekkRequestBuilder oppdragsvedtak(Oppdragsvedtak... oppdragsvedtakList) {
        if (this.oppdragsvedtakList == null) {
            this.oppdragsvedtakList = new ArrayList<>();
        }
        this.oppdragsvedtakList.addAll(Arrays.asList(oppdragsvedtakList));
        return this;
    }

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
        trekkRequest.setAntallDager(antallDager);
        trekkRequest.setTrekkvedtakId(trekkvedtakId);
        trekkRequest.getOppdragsvedtak().addAll(oppdragsvedtakList);
        return trekkRequest;
    }

}
