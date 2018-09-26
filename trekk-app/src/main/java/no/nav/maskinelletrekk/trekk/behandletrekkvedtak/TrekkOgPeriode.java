package no.nav.maskinelletrekk.trekk.behandletrekkvedtak;

import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

public final class TrekkOgPeriode {

    private LocalDate fom;
    private LocalDate tom;
    private List<TrekkRequest> trekkRequestList;

    TrekkOgPeriode(TrekkRequest trekkRequest) {
        this(Collections.singletonList(trekkRequest));
    }

    public TrekkOgPeriode(List<TrekkRequest> trekkRequestList) {
        this.trekkRequestList = trekkRequestList;
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        this.fom = nextMonth.atDay(1);
        this.tom = nextMonth.atEndOfMonth();
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public List<TrekkRequest> getTrekkRequestList() {
        return trekkRequestList;
    }

}
