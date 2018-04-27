package no.nav.maskinelletrekk.trekk.v1.builder;

import no.nav.maskinelletrekk.trekk.v1.Oppdragsvedtak;
import no.nav.maskinelletrekk.trekk.v1.Periode;
import no.nav.maskinelletrekk.trekk.v1.TypeSats;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OppdragsvedtakBuilder {

    private BigDecimal sats;
    private TypeSats typeSats;
    private Periode periode;

    public static OppdragsvedtakBuilder create() {
        return new OppdragsvedtakBuilder();
    }

    public OppdragsvedtakBuilder sats(BigDecimal sats) {
        this.sats = sats;
        return this;
    }

    public OppdragsvedtakBuilder typeSats(TypeSats typeSats) {
        this.typeSats = typeSats;
        return this;
    }

    public OppdragsvedtakBuilder periode(Periode periode) {
        this.periode = periode;
        return this;
    }

    public OppdragsvedtakBuilder periode(LocalDate fom, LocalDate tom) {
        periode = new Periode();
        periode.setFom(fom);
        periode.setTom(tom);
        return this;
    }

    public Oppdragsvedtak build() {
        Oppdragsvedtak oppdragsvedtak = new Oppdragsvedtak();
        oppdragsvedtak.setSats(sats);
        oppdragsvedtak.setTypeSats(typeSats);
        oppdragsvedtak.setPeriode(periode);
        return oppdragsvedtak;
    }

}
