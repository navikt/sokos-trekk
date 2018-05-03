package no.nav.maskinelletrekk.trekk.ytelsevedtak;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Periode;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Sak;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Tema;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Vedtak;
import org.junit.Test;

import java.util.Collections;
import java.util.stream.Stream;

public class SakTilVedtakMapperTest {

    private SakTilVedtakMapper mapper = new SakTilVedtakMapper();

    @Test
    public void skalHaandtereTomDato() {
        Sak sak = new Sak();
        Tema tema = new Tema();
        tema.setKodeverksRef("tema1");
        sak.setFagsystemSakId("fag1");
        Vedtak vedtak = new Vedtak();
        vedtak.setDagsats(123.2);

        Periode periode = new Periode();
        periode.setFom(null);
        periode.setTom(null);
        vedtak.setVedtaksperiode(periode);

        sak.getVedtakListe().addAll(Collections.singletonList(
                vedtak
        ));
        sak.setTema(tema);
        Stream<? extends ArenaVedtak> apply = mapper.apply(sak);
    }

}