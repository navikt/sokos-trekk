package no.nav.maskinelletrekk.trekk.behandletrekkvedtak;

import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;

import java.time.LocalDate;
import java.time.YearMonth;

public final class TrekkRequestOgPeriode {

    private TrekkRequest trekkRequest;
    private LocalDate fom;
    private LocalDate tom;

    public TrekkRequestOgPeriode(TrekkRequest trekkRequest) {
        this.trekkRequest = trekkRequest;
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        this.fom = nextMonth.atDay(1);
        this.tom = nextMonth.atEndOfMonth();

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
