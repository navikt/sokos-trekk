package no.nav.maskinelletrekk.trekk.arenamock;

import no.nav.maskinelletrekk.trekk.behandletrekkvedtak.TrekkOgPeriode;
import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.Periode;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.builder.PeriodeBuilder;
import no.nav.maskinelletrekk.trekk.ytelsevedtak.YtelseVedtakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArenaMockService implements YtelseVedtakService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArenaMockService.class);

    private String mockDataXml;
    private LocalDate kjoreDato;
    private Map<String, List<ArenaVedtak>> mockDataMap;

    @Override
    public Map<String, List<ArenaVedtak>> hentYtelseskontrakt(TrekkOgPeriode trekkOgPeriode) {
        Map<String, List<ArenaVedtak>> vedtakMap = new HashMap<>();
        Periode periode;
        if (kjoreDato != null) {
            LOGGER.info("ARENA-MOCK: Kjøredato er satt til {}", kjoreDato);
            periode = periode(
                    YearMonth.of(kjoreDato.getYear(), kjoreDato.getMonth()).plusMonths(1).atDay(1),
                    YearMonth.of(kjoreDato.getYear(), kjoreDato.getMonth()).plusMonths(1).atEndOfMonth());
        } else {
            periode = periode(trekkOgPeriode.getFom(), trekkOgPeriode.getTom());
        }
        LOGGER.info("ARENA-MOCK: Bruker perioden {} til {}", periode.getFom(), periode.getTom());
        if (mockDataMap != null) {
            for (TrekkRequest trekkRequest : trekkOgPeriode.getTrekkRequestList()) {
                String fnr = trekkRequest.getBruker();
                if (mockDataMap.containsKey(fnr)) {
                    vedtakMap.put(fnr, hentArenaVedtakListe(fnr, periode));
                }
            }
        } else {
            LOGGER.error("Mangler testdata!");
        }
        return vedtakMap;
    }

    private List<ArenaVedtak> hentArenaVedtakListe(String fnr, Periode periode) {
        List<ArenaVedtak> arenaVedtakListe = new ArrayList<>();
        for (ArenaVedtak arenaVedtak : mockDataMap.get(fnr)) {
            if (PeriodeSjekk.erInnenforPeriode(arenaVedtak.getVedtaksperiode(), periode)) {
                arenaVedtakListe.add(arenaVedtak);
            }
        }
        return arenaVedtakListe;
    }

    private Periode periode(LocalDate fom, LocalDate tom) {
        return PeriodeBuilder.create()
                .fom(fom)
                .tom(tom).build();
    }

    public String getMockDataXml() {
        return mockDataXml;
    }

    public void setMockDataXml(String mockDataXml) {
        this.mockDataXml = mockDataXml;
    }

    public void setKjoreDato(LocalDate kjoreDato) {
        this.kjoreDato = kjoreDato;
    }

    public Map<String, List<ArenaVedtak>> getMockDataMap() {
        return mockDataMap;
    }

    public void setMockDataMap(Map<String, List<ArenaVedtak>> mockDataMap) {
        this.mockDataMap = mockDataMap;
    }

}
