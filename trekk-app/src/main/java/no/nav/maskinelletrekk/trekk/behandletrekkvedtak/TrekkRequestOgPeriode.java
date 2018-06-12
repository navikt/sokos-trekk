package no.nav.maskinelletrekk.trekk.behandletrekkvedtak;

import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;

import java.time.LocalDate;

public final class TrekkRequestOgPeriode {

    private TrekkRequest trekkRequest;
    private LocalDate fom;
    private LocalDate tom;

    public TrekkRequestOgPeriode(TrekkRequest trekkRequest) {
        this.trekkRequest = trekkRequest;
        LocalDate now = LocalDate.now();
        this.fom = now;
        this.tom = now.plusDays(trekkRequest.getAntallDager());
    }

    public TrekkRequest getTrekkRequest() {
        return trekkRequest;
    }

    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }
}
