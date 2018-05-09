package no.nav.maskinelletrekk.trekk.arenamock;

import no.nav.maskinelletrekk.arenamock.v1.ArenaMockData;
import no.nav.maskinelletrekk.arenamock.v1.PersonYtelse;
import no.nav.maskinelletrekk.trekk.helper.XmlHelper;
import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.builder.TrekkRequestBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
                .antallDager(60)
                .bruker(BRUKER)
                .build();
        Map<String, List<ArenaVedtak>> map = service.hentYtelseskontrakt(Collections.singletonList(request));

        List<ArenaVedtak> arenaVedtakList = map.get(BRUKER);
        Assert.assertThat(arenaVedtakList.size(), equalTo(4));
        Assert.assertThat(arenaVedtakList.get(0).getDagsats().doubleValue(), equalTo(13.0));
        Assert.assertThat(arenaVedtakList.get(1).getDagsats().doubleValue(), equalTo(14.0));
        Assert.assertThat(arenaVedtakList.get(2).getDagsats().doubleValue(), equalTo(15.0));
        Assert.assertThat(arenaVedtakList.get(3).getDagsats().doubleValue(), equalTo(16.0));
    }

    private Map<String, List<ArenaVedtak>> stubArenaMockData() {
        ArenaMockData mockData = XmlHelper.getMockDataFromXml("arenaMockDateTest.xml");
        service.setKjoreDato(mockData.getKjoreDato());
        return mockData.getPersonYtelse().stream()
                .collect(Collectors.toMap(PersonYtelse::getIdent, new ArenaMockDataMapper()));
    }

}