package no.nav.maskinelletrekk.trekk.behandletrekkvedtak;

import no.nav.maskinelletrekk.trekk.helper.XmlHelper;
import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.Beslutning;
import no.nav.maskinelletrekk.trekk.v1.Trekk;
import no.nav.maskinelletrekk.trekk.v1.TrekkResponse;
import no.nav.maskinelletrekk.trekk.v1.builder.ArenaVedtakBuilder;
import no.nav.maskinelletrekk.trekk.ytelsevedtak.YtelseVedtakService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anySet;

@RunWith(MockitoJUnitRunner.class)
public class BehandleTrekkvedtakBeanTest {

    private static final String TREKK_V1_REQUEST_1_XML = "trekkrequest_testcase1.xml";
    private static final String TREKK_V1_REQUEST_2_XML = "trekkrequest_testcase2.xml";

    private static final int ANTALL_DAGER = 60;
    private static final String FNR_1 = "12345678901";
    private static final String FNR_2 = "10987654321";
    private static final int TREKKVEDTAK_ID = 1;
    private static final LocalDate FOM_PERIODE_1 = LocalDate.of(2018, 1, 1);
    private static final LocalDate TOM_PERIODE_1 = LocalDate.of(2018, 1, 31);
    private static final LocalDate FOM_PERIODE_2 = LocalDate.of(2018, 2, 1);
    private static final LocalDate TOM_PERIODE_2 = LocalDate.of(2018, 2, 28);
    private static final BigDecimal DAGSATS_1 = new BigDecimal("123.43");
    private static final BigDecimal DAGSATS_2 = new BigDecimal("32.43");
    private static final BigDecimal DAGSATS_3 = new BigDecimal("12332.43");
    private static final BigDecimal DAGSATS_4 = new BigDecimal("43332.43");
    private static final String RETTIGHET_TYPE = "AAP";
    private static final String TEMA = "AAP";

    @Mock
    private YtelseVedtakService ytelseVedtakService;

    @Mock
    private Clock clock;

    @InjectMocks
    private BehandleTrekkvedtakBean behandleTrekkvedtak;

    private Trekk requestFromXml;

    @Before
    public void setUp() throws Exception {
        requestFromXml = XmlHelper.getRequestFromXml(TREKK_V1_REQUEST_1_XML);

        Mockito.when(clock.instant()).thenReturn(Instant.parse("2019-01-01T10:00:00Z"));
        Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    @Test
    public void skalBeslutteOSDersomSumOSErStorreEnnSumArena() {
        Map<String, List<ArenaVedtak>> tid = opprettSvar(FNR_1, DAGSATS_1, DAGSATS_2);
        Mockito.when(ytelseVedtakService.hentYtelseskontrakt(anySet(), any(LocalDate.class), any(LocalDate.class))).thenReturn(tid);

        Trekk trekk = behandleTrekkvedtak.behandleTrekkvedtak(requestFromXml);

        TrekkResponse trekkResponse = trekk.getTrekkResponse().get(0);
        assertThat(trekkResponse.getBeslutning(), equalTo(Beslutning.OS));
        assertThat(trekk.getTrekkResponse().size(), equalTo(2));
    }

    @Test
    public void skalReturnereBesluttningIngenVedIngenVedtakFraOSOgIngenVedtakFraArena() throws Exception {

        requestFromXml = XmlHelper.getRequestFromXml(TREKK_V1_REQUEST_2_XML);

        Map<String, List<ArenaVedtak>> tid = opprettSvar(FNR_2);
        Mockito.when(ytelseVedtakService.hentYtelseskontrakt(anySet(), any(LocalDate.class), any(LocalDate.class))).thenReturn(tid);

        Trekk trekk = behandleTrekkvedtak.behandleTrekkvedtak(requestFromXml);

        TrekkResponse trekkResponse1 = trekk.getTrekkResponse().get(0);
        TrekkResponse trekkResponse2 = trekk.getTrekkResponse().get(1);
        assertThat(trekkResponse1.getBeslutning(), equalTo(Beslutning.INGEN));
        assertThat(trekkResponse2.getBeslutning(), equalTo(Beslutning.INGEN));
        assertThat(trekk.getTrekkResponse().size(), equalTo(2));
    }

    @Test
    public void verifiserFomTom() {


        ArgumentCaptor <LocalDate> fomCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor <LocalDate> tomCaptor = ArgumentCaptor.forClass(LocalDate.class);

        behandleTrekkvedtak.behandleTrekkvedtak(requestFromXml);

        Mockito.verify(ytelseVedtakService).hentYtelseskontrakt(anySet(), fomCaptor.capture(), tomCaptor.capture());

        assertEquals(fomCaptor.getValue(), LocalDate.now(clock));
        assertEquals(tomCaptor.getValue(), YearMonth.now(clock).plusMonths(1).atEndOfMonth());
    }

    private Map<String, List<ArenaVedtak>> opprettSvar(String fnr, BigDecimal... dagsatser) {

        List<ArenaVedtak> arenaVedtakList = new ArrayList<>();
        Map<String, List<ArenaVedtak>> arenaVedtakMap = new HashMap<>();

        for (BigDecimal dagsats : dagsatser) {
            arenaVedtakList.add(ArenaVedtakBuilder.create()
                    .dagsats(dagsats)
                    .rettighetType(RETTIGHET_TYPE)
                    .tema(TEMA)
                    .vedtaksperiode(FOM_PERIODE_1, TOM_PERIODE_1)
                    .build());
        }
        arenaVedtakMap.put(fnr, arenaVedtakList);
        return arenaVedtakMap;
    }

}