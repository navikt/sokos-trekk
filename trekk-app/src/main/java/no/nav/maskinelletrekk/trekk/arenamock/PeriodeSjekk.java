package no.nav.maskinelletrekk.trekk.arenamock;

import no.nav.maskinelletrekk.trekk.v1.Periode;

import java.time.LocalDate;

public final class PeriodeSjekk {

    private PeriodeSjekk() {
    }

    static boolean erInnenforPeriode(Periode arenaVedtaksPeriode, Periode requestPeriode) {
        if (arenaVedtaksPeriode.getTom() == null) {
            return arenaVedtaksPeriode.getFom().isBefore(requestPeriode.getTom())
                    || arenaVedtaksPeriode.getFom().isEqual(requestPeriode.getTom());
        } else {
            return erVedtakPeriodeStorreEnnRequestPeriode(arenaVedtaksPeriode.getFom(), arenaVedtaksPeriode.getTom(), requestPeriode)
                    || erVedtakDatoInnenforRequestPeriode(arenaVedtaksPeriode.getTom(), requestPeriode)
                    || erVedtakDatoInnenforRequestPeriode(arenaVedtaksPeriode.getFom(), requestPeriode);
        }
    }

    private static boolean erVedtakPeriodeStorreEnnRequestPeriode(LocalDate fom, LocalDate tom, Periode requestPeriode) {
        return fom.isBefore(requestPeriode.getFom())
                && (tom.isAfter(requestPeriode.getFom()));
    }

    private static boolean erVedtakDatoInnenforRequestPeriode(LocalDate dato, Periode requestPeriode) {
        return (dato.isAfter(requestPeriode.getFom()) || dato.isEqual(requestPeriode.getFom()))
                && (dato.isBefore(requestPeriode.getTom()) || dato.isEqual(requestPeriode.getTom()));
    }
}
