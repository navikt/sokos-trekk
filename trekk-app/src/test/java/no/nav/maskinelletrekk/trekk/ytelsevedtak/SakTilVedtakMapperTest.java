package no.nav.maskinelletrekk.trekk.ytelsevedtak;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Periode;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Rettighetstype;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Sak;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Tema;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Vedtak;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SakTilVedtakMapperTest {

    private static final double DAGSATS_1 = 123.2;
    private static final double DAGSATS_2 = 431;
    private static final String FAGSYSTEM_SAK_ID = "fag1";
    private static final String RETTIGHETSTYPE_1 = "RT1";
    private static final String RETTIGHETSTYPE_2 = "443";
    private static final String TEMA = "tema1";
    private static final int JANUARY = 1;
    private static final int FEBRUARY = 2;
    private static final LocalDate FOM_1 = LocalDate.of(2018, JANUARY, 1);
    private static final LocalDate TOM_1 = LocalDate.of(2018, JANUARY, 31);
    private static final LocalDate FOM_2 = LocalDate.of(2018, FEBRUARY, 1);
    private static final LocalDate TOM_2 = LocalDate.of(2018, FEBRUARY, 28);

    private SakTilVedtakMapper mapper = new SakTilVedtakMapper();

    @Test
    public void skalMappeFraSakTilArenaVedtak() throws Exception {

        Vedtak vedtak1 = new Vedtak();
        vedtak1.setDagsats(DAGSATS_1);
        vedtak1.setRettighetstype(opprettRettighetstype(RETTIGHETSTYPE_1));
        vedtak1.setVedtaksperiode(opprettPeriode(FOM_1, TOM_1));

        Vedtak vedtak2 = new Vedtak();
        vedtak2.setDagsats(DAGSATS_2);
        vedtak2.setRettighetstype(opprettRettighetstype(RETTIGHETSTYPE_2));
        vedtak2.setVedtaksperiode(opprettPeriode(FOM_2, TOM_2));

        Sak sak = new Sak();
        sak.setFagsystemSakId(FAGSYSTEM_SAK_ID);
        sak.setTema(opprettTema(TEMA));
        sak.getVedtakListe().addAll(Arrays.asList(
                vedtak1,
                vedtak2
        ));

        Stream<? extends ArenaVedtak> arenaVedtakStream = mapper.apply(sak);

        List<ArenaVedtak> arenaVedtakList = arenaVedtakStream.collect(Collectors.toList());

        assertThat(arenaVedtakList.size(), equalTo(2));

        ArenaVedtak arenaVedtak1 = arenaVedtakList.get(0);
        ArenaVedtak arenaVedtak2 = arenaVedtakList.get(1);

        assertThat(arenaVedtak1.getDagsats().doubleValue(), equalTo(DAGSATS_1));
        assertThat(arenaVedtak1.getRettighetType(), equalTo(RETTIGHETSTYPE_1));
        assertThat(arenaVedtak1.getTema(), equalTo(TEMA));
        assertThat(arenaVedtak1.getVedtaksperiode().getFom(), equalTo(FOM_1));
        assertThat(arenaVedtak1.getVedtaksperiode().getTom(), equalTo(TOM_1));
        assertThat(arenaVedtak2.getDagsats().doubleValue(), equalTo(DAGSATS_2));
        assertThat(arenaVedtak2.getRettighetType(), equalTo(RETTIGHETSTYPE_2));
        assertThat(arenaVedtak2.getTema(), equalTo(TEMA));
        assertThat(arenaVedtak2.getVedtaksperiode().getFom(), equalTo(FOM_2));
        assertThat(arenaVedtak2.getVedtaksperiode().getTom(), equalTo(TOM_2));
    }

    @Test
    public void skalHaandtereTomDato() throws Exception {

        Periode periode = new Periode();
        periode.setFom(null);
        periode.setTom(null);

        Vedtak vedtak1 = new Vedtak();
        vedtak1.setDagsats(DAGSATS_1);
        vedtak1.setRettighetstype(opprettRettighetstype(RETTIGHETSTYPE_1));
        vedtak1.setVedtaksperiode(opprettPeriode(null, null));

        Sak sak = new Sak();
        sak.setFagsystemSakId(FAGSYSTEM_SAK_ID);
        sak.setTema(opprettTema(TEMA));
        sak.getVedtakListe().addAll(Collections.singletonList(
                vedtak1
        ));

        Stream<? extends ArenaVedtak> apply = mapper.apply(sak);

        List<ArenaVedtak> list = apply.collect(Collectors.toList());

        ArenaVedtak arenaVedtak = list.get(0);
        assertThat(list.size(), equalTo(1));
        assertThat(arenaVedtak.getVedtaksperiode().getFom(), CoreMatchers.nullValue());
        assertThat(arenaVedtak.getVedtaksperiode().getTom(), CoreMatchers.nullValue());
    }

    private Tema opprettTema(String tema1) {
        Tema tema = new Tema();
        tema.setKodeverksRef(tema1);
        return tema;
    }

    private Rettighetstype opprettRettighetstype(String rettighetstype11) {
        Rettighetstype rettighetstype1 = new Rettighetstype();
        rettighetstype1.setKodeverksRef(rettighetstype11);
        return rettighetstype1;
    }

    private Periode opprettPeriode(LocalDate fom, LocalDate tom) throws DatatypeConfigurationException {
        Periode periode = new Periode();
        periode.setFom(DateUtil.toXmlGregorianCalendar(fom));
        periode.setTom(DateUtil.toXmlGregorianCalendar(tom));
        return periode;
    }

}