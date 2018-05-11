package no.nav.maskinelletrekk.trekk.arenamock;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.Periode;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.builder.PeriodeBuilder;
import no.nav.maskinelletrekk.trekk.ytelsevedtak.YtelseVedtakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    public Map<String, List<ArenaVedtak>> hentYtelseskontrakt(List<TrekkRequest> trekkRequestListe) {
        Map<String, List<ArenaVedtak>> vedtakMap = new HashMap<>();
        if (mockDataMap != null) {
            for (TrekkRequest trekkRequest : trekkRequestListe) {
                String fnr = trekkRequest.getBruker();
                int antallDager = trekkRequest.getAntallDager();
                if (mockDataMap.containsKey(fnr)) {
                    vedtakMap.put(fnr, hentArenaVedtakListe(fnr, antallDager));
                }
            }
        } else {
            LOGGER.error("Mangler testdata!");
        }
        return vedtakMap;
    }

    private List<ArenaVedtak> hentArenaVedtakListe(String fnr, int antallDager) {
        List<ArenaVedtak> arenaVedtakListe = new ArrayList<>();
        for (ArenaVedtak arenaVedtak : mockDataMap.get(fnr)) {
            if (isGyldigPeriode(arenaVedtak.getVedtaksperiode(), antallDager)) {
                arenaVedtakListe.add(arenaVedtak);
            }
        }
        return arenaVedtakListe;
    }

    private boolean isGyldigPeriode(Periode periode, int antallDager) {
        Periode requestPeriode = PeriodeBuilder.create()
                .fom(kjoreDato)
                .tom(kjoreDato.plusDays(antallDager)).build();
        return erVedtakDatoInnenforRequestPeriode(periode.getTom(), requestPeriode)
                || erVedtakDatoInnenforRequestPeriode(periode.getFom(), requestPeriode)
                || erVedtakPeriodeStorreEnnRequestPeriode(periode.getFom(), periode.getTom(), requestPeriode);
    }

    private boolean erVedtakDatoInnenforRequestPeriode(LocalDate dato, Periode requestPeriode) {
        return (dato.isAfter(requestPeriode.getFom()) || dato.isEqual(requestPeriode.getFom()))
                && (dato.isBefore(requestPeriode.getTom()) || dato.isEqual(requestPeriode.getTom()));
    }

    private boolean erVedtakPeriodeStorreEnnRequestPeriode(LocalDate fom, LocalDate tom, Periode requestPeriode) {
        return fom.isBefore(requestPeriode.getFom())
                && tom.isAfter(requestPeriode.getFom());
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
