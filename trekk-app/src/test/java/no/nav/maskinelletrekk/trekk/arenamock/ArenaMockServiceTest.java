package no.nav.maskinelletrekk.trekk.arenamock;

import no.nav.maskinelletrekk.arenamock.v1.ArenaMockData;
import no.nav.maskinelletrekk.arenamock.v1.MockPeriode;
import no.nav.maskinelletrekk.arenamock.v1.MockVedtak;
import no.nav.maskinelletrekk.arenamock.v1.PersonYtelse;
import no.nav.maskinelletrekk.trekk.behandletrekkvedtak.TrekkOgPeriode;
import no.nav.maskinelletrekk.trekk.helper.XmlHelper;
import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.builder.TrekkRequestBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;

public class ArenaMockServiceTest {

    private static final String BRUKER = "13245678901";
    private static final int TREKKVEDTAK_ID = 1;
    private ArenaMockService service = new ArenaMockService();

    @Before
    public void setUp() {
        service = new ArenaMockService();
        service.setMockDataMap(stubArenaMockData());
    }

    @Test
    public void skalReturnereGyldigeVedtak() {
        TrekkRequest request = TrekkRequestBuilder.create()
                .trekkvedtakId(TREKKVEDTAK_ID)
                .bruker(BRUKER)
                .build();
        Map<String, List<ArenaVedtak>> map = service.hentYtelseskontrakt(new TrekkOgPeriode(Collections.singletonList(request)));

        List<ArenaVedtak> arenaVedtakList = map.get(BRUKER);
        Assert.assertThat(arenaVedtakList.size(), equalTo(6));
        Assert.assertThat(arenaVedtakList.get(0).getDagsats().doubleValue(), equalTo(12.0));
        Assert.assertThat(arenaVedtakList.get(1).getDagsats().doubleValue(), equalTo(13.0));
        Assert.assertThat(arenaVedtakList.get(2).getDagsats().doubleValue(), equalTo(14.0));
        Assert.assertThat(arenaVedtakList.get(3).getDagsats().doubleValue(), equalTo(15.0));
    }

    private Map<String, List<ArenaVedtak>> stubArenaMockData() {
        ArenaMockData mockData = XmlHelper.getMockDataFromXml("arenaMockDateTest.xml");
        MockPeriode periode = new MockPeriode();
        periode.setFom(YearMonth.now().plusMonths(1).atDay(4));
        periode.setTom(YearMonth.now().plusMonths(1).atDay(5));
        for (MockVedtak mockVedtak : mockData.getPersonYtelse().get(0).getSak().get(0).getVedtak()) {
            mockVedtak.setVedtaksperiode(periode);
        }
        service.setKjoreDato(mockData.getKjoreDato());
        return mockData.getPersonYtelse().stream()
                .collect(Collectors.toMap(PersonYtelse::getIdent, new ArenaMockDataMapper()));
    }

}