package no.nav.maskinelletrekk.trekk.ytelsevedtak;

import no.nav.maskinelletrekk.trekk.helper.XmlHelper;
import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.Trekk;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.TypeSats;
import no.nav.maskinelletrekk.trekk.v1.builder.OppdragsvedtakBuilder;
import no.nav.maskinelletrekk.trekk.v1.builder.TrekkRequestBuilder;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.YtelseVedtakV1;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Periode;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.PersonYtelse;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Rettighetstype;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Sak;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Tema;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Vedtak;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeRequest;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArenaYtelseVedtakServiceTest {

    private static final String PERSON_IDENT_1 = "12345678901";
    private static final String PERSON_IDENT_2 = "10987654321";
    private static final int TREKKVEDTAK_ID_1 = 11;
    private static final int TREKKVEDTAK_ID_2 = 22;
    private static final String FAGSYSTEM_SAK_ID_1 = "fagsystemsakid1";
    private static final String TEMA_1 = "tema1";
    private static final double DAGSATS_1 = 123.32;
    private static final double DAGSATS_2 = 432.42;
    private static final String RETTIGHETSTYPE_1 = "rettigheten1";
    private static final String RETTIGHETSTYPE_2 = "rettigheten2";
    private static final LocalDate FOM_PERIODE_1 = LocalDate.of(2018, 1, 1);
    private static final LocalDate TOM_PERIODE_1 = LocalDate.of(2018, 1, 31);
    private static final LocalDate FOM_PERIODE_2 = LocalDate.of(2018, 2, 1);
    private static final LocalDate TOM_PERIODE_2 = LocalDate.of(2018, 2, 28);
    private static final int ANTALL_DAGER_1 = 60;
    private static final int ANTALL_DAGER_2 = 50;
    private static final Instant NOW = Instant.parse("2017-12-03T10:15:30.00Z");
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    private static final String TREKK_V1_REQUEST_XML = "trekkrequest_testcase1.xml";

    @Mock
    private YtelseVedtakV1 ytelseVedtakService;

    @Spy
    private SakTilVedtakMapper sakTilVedtakMapper;

    @Mock
    private Clock clock;

    @Captor
    private ArgumentCaptor<FinnYtelseVedtakListeRequest> requestCaptor;

    @InjectMocks
    private ArenaYtelseVedtakService service;

    @Before
    public void setUp() {
        when(clock.instant()).thenReturn(NOW);
        when(clock.getZone()).thenReturn(DEFAULT_ZONE);
    }

    @Test
    public void testFlatMap() throws Exception {
        FinnYtelseVedtakListeResponse svarFraYtelseVedtakService = new FinnYtelseVedtakListeResponse();

        PersonYtelse person = new PersonYtelse();
        svarFraYtelseVedtakService.getPersonYtelseListe().add(person);
        person.setIdent(PERSON_IDENT_1);

        Tema tema = new Tema();
        tema.setKodeverksRef(TEMA_1);

        Sak sak1 = new Sak();
        sak1.setFagsystemSakId(FAGSYSTEM_SAK_ID_1);
        sak1.setTema(tema);

        Rettighetstype rettighetstype = new Rettighetstype();
        rettighetstype.setKodeverksRef(RETTIGHETSTYPE_1);

        Periode periode1 = new Periode();
        periode1.setFom(DateUtil.toXmlGregorianCalendar(FOM_PERIODE_1));
        periode1.setTom(DateUtil.toXmlGregorianCalendar(TOM_PERIODE_1));

        Periode periode2 = new Periode();
        periode2.setFom(DateUtil.toXmlGregorianCalendar(FOM_PERIODE_2));
        periode2.setTom(DateUtil.toXmlGregorianCalendar(TOM_PERIODE_2));


        Vedtak vedtaket = new Vedtak();
        vedtaket.setDagsats(DAGSATS_1);
        vedtaket.setRettighetstype(rettighetstype);
        vedtaket.setVedtaksperiode(periode1);
        sak1.getVedtakListe().add(vedtaket);

        Rettighetstype rettighetstype2 = new Rettighetstype();
        rettighetstype2.setKodeverksRef(RETTIGHETSTYPE_2);

        Vedtak vedtaket2 = new Vedtak();
        vedtaket2.setDagsats(DAGSATS_2);
        vedtaket2.setRettighetstype(rettighetstype2);
        vedtaket2.setVedtaksperiode(periode2);
        sak1.getVedtakListe().add(vedtaket2);

        person.getSakListe().add(sak1);

        when(ytelseVedtakService.finnYtelseVedtakListe(any(FinnYtelseVedtakListeRequest.class))).thenReturn(svarFraYtelseVedtakService);

        List<TrekkRequest> trekkRequestList = Arrays.asList(
                TrekkRequestBuilder.create()
                        .antallDager(ANTALL_DAGER_1)
                        .bruker(PERSON_IDENT_1)
                        .trekkvedtakId(TREKKVEDTAK_ID_1)
                        .oppdragsvedtak(
                                OppdragsvedtakBuilder.create()
                                        .sats(BigDecimal.valueOf(DAGSATS_1))
                                        .typeSats(TypeSats.DAG)
                                        .periode(FOM_PERIODE_1, TOM_PERIODE_1)
                                        .build(),
                                OppdragsvedtakBuilder.create()
                                        .sats(BigDecimal.valueOf(DAGSATS_2))
                                        .typeSats(TypeSats.MND)
                                        .periode(FOM_PERIODE_2, TOM_PERIODE_2)
                                        .build()
                        )
                        .build(),
                TrekkRequestBuilder.create()
                        .antallDager(ANTALL_DAGER_2)
                        .bruker(PERSON_IDENT_2)
                        .trekkvedtakId(TREKKVEDTAK_ID_2)
                        .oppdragsvedtak(
                                OppdragsvedtakBuilder.create()
                                        .sats(BigDecimal.valueOf(DAGSATS_1))
                                        .typeSats(TypeSats.DAG)
                                        .periode(FOM_PERIODE_1, TOM_PERIODE_1)
                                        .build(),
                                OppdragsvedtakBuilder.create()
                                        .sats(BigDecimal.valueOf(DAGSATS_2))
                                        .typeSats(TypeSats.MND)
                                        .periode(FOM_PERIODE_2, TOM_PERIODE_2)
                                        .build()
                        )
                        .build()
        );



        Map<String, List<ArenaVedtak>> stringArenaYtelseVedtakMap = service.hentYtelseskontrakt(trekkRequestList);

        Assert.assertNotNull(stringArenaYtelseVedtakMap);
    }

    @Test
    public void skalKonvertereTrekkRequestTilPersonListeMedIdentOgPeriode() throws Exception {
        when(ytelseVedtakService.finnYtelseVedtakListe(any())).thenReturn(new FinnYtelseVedtakListeResponse());
        Trekk requestFromXml = XmlHelper.getRequestFromXml(TREKK_V1_REQUEST_XML);

        service.hentYtelseskontrakt(requestFromXml.getTrekkRequest());
        verify(ytelseVedtakService).finnYtelseVedtakListe(requestCaptor.capture());

        Person person1 = requestCaptor.getValue().getPersonListe().get(0);
        assertThat(person1.getIdent(), equalTo(PERSON_IDENT_1));
        assertThat(person1.getPeriode().getFom(), equalTo(DateUtil.toXmlGregorianCalendar(LocalDateTime.ofInstant(NOW, DEFAULT_ZONE).toLocalDate())));
        assertThat(person1.getPeriode().getTom(), equalTo(DateUtil.toXmlGregorianCalendar(LocalDateTime.ofInstant(NOW, DEFAULT_ZONE).plusDays(ANTALL_DAGER_1).toLocalDate())));
    }

    @Test
    public void skalReturnereTomListeDersomSvarFraArenaErTomt2() throws Exception {
        when(ytelseVedtakService.finnYtelseVedtakListe(any()))
                .thenReturn(new FinnYtelseVedtakListeResponse());

        Map<String, List<ArenaVedtak>> arenaYtelseVedtakMap = service.hentYtelseskontrakt(
                XmlHelper.getRequestFromXml(TREKK_V1_REQUEST_XML).getTrekkRequest());

        assertThat(arenaYtelseVedtakMap, notNullValue());
        assertThat(arenaYtelseVedtakMap.size(), is(0));
    }

}