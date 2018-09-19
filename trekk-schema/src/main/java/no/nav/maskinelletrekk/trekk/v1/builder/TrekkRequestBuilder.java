package no.nav.maskinelletrekk.trekk.v1.builder;

import no.nav.maskinelletrekk.trekk.v1.ObjectFactory;
import no.nav.maskinelletrekk.trekk.v1.System;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.Trekkalternativ;

import java.math.BigDecimal;

public final class TrekkRequestBuilder {

    private String bruker;
    private int antallDager;
    private int trekkvedtakId;
    private Trekkalternativ trekkalt;
    private System system;
    private BigDecimal totalSatsOS;

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

    public TrekkRequestBuilder trekkalt(Trekkalternativ trekkalt) {
        this.trekkalt = trekkalt;
        return this;
    }

    public TrekkRequestBuilder system(System system) {
        this.system = system;
        return this;
    }
    public TrekkRequestBuilder totalSatsOS(BigDecimal totalSatsOS) {
        this.totalSatsOS = totalSatsOS;
        return this;
    }

    public TrekkRequest build() {
        TrekkRequest trekkRequest = new ObjectFactory().createTrekkRequest();
        trekkRequest.setBruker(bruker);
        trekkRequest.setAntallDager(antallDager);
        trekkRequest.setTrekkvedtakId(trekkvedtakId);
        trekkRequest.setTrekkalt(trekkalt);
        trekkRequest.setSystem(system);
        trekkRequest.setTotalSatsOS(totalSatsOS);
        return trekkRequest;
    }

}
