package no.nav.maskinelletrekk.trekk.v1.builder;

import no.nav.maskinelletrekk.trekk.v1.Periode;

import java.time.LocalDate;

public class PeriodeBuilder {
    private LocalDate fom;
    private LocalDate tom;

    private PeriodeBuilder() {
    }

    public static PeriodeBuilder create() {
        return new PeriodeBuilder();
    }

    public PeriodeBuilder fom(LocalDate fom) {
        this.fom = fom;
        return this;
    }

    public PeriodeBuilder tom(LocalDate tom) {
        this.tom = tom;
        return this;
    }

    public Periode build() {
        Periode periode = new Periode();
        periode.setFom(fom);
        periode.setTom(tom);
        return periode;
    }
}
