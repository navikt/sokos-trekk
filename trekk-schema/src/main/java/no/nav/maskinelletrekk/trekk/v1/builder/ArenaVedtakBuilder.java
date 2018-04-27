package no.nav.maskinelletrekk.trekk.v1.builder;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.Periode;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ArenaVedtakBuilder {

    private BigDecimal dagsats;
    private String tema;
    private String rettighetType;
    private Periode vedtaksperiode;

    public static ArenaVedtakBuilder create() {
        return new ArenaVedtakBuilder();
    }

    public ArenaVedtakBuilder dagsats(BigDecimal dagsats) {
        this.dagsats = dagsats;
        return this;
    }

    public ArenaVedtakBuilder tema(String tema) {
        this.tema = tema;
        return this;
    }

    public ArenaVedtakBuilder rettighetType(String rettighetType) {
        this.rettighetType = rettighetType;
        return this;
    }

    public ArenaVedtakBuilder vedtaksperiode(Periode vedtaksperiode) {
        this.vedtaksperiode = vedtaksperiode;
        return this;
    }

    public ArenaVedtakBuilder vedtaksperiode(LocalDate fom, LocalDate tom) {
        Periode periode = new Periode();
        periode.setFom(fom);
        periode.setTom(tom);
        this.vedtaksperiode = periode;
        return this;
    }


    public ArenaVedtak build() {
        ArenaVedtak vedtak = new ArenaVedtak();
        vedtak.setDagsats(dagsats);
        vedtak.setTema(tema);
        vedtak.setRettighetType(rettighetType);
        vedtak.setVedtaksperiode(vedtaksperiode);
        return vedtak;
    }

}
