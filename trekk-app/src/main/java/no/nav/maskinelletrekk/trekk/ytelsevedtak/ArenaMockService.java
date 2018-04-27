package no.nav.maskinelletrekk.trekk.ytelsevedtak;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.builder.ArenaVedtakBuilder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArenaMockService implements YtelseVedtakService {
    @Override
    public Map<String, List<ArenaVedtak>> hentYtelseskontrakt(List<TrekkRequest> trekkRequestListe) {
        Map<String, List<ArenaVedtak>> mockData = new HashMap<>();
        mockData.put("12345678901", Arrays.asList(
                ArenaVedtakBuilder.create()
                        .dagsats(BigDecimal.valueOf(1234.50))
                        .tema("DAG")
                        .rettighetType("DAGO")
                        .vedtaksperiode(LocalDate.now(), LocalDate.now().plusDays(1))
                        .build(),
                ArenaVedtakBuilder.create()
                        .dagsats(BigDecimal.valueOf(120.00))
                        .tema("DAG")
                        .rettighetType("DAGO")
                        .vedtaksperiode(LocalDate.now(), LocalDate.now().plusDays(1))
                        .build()
        ));
        mockData.put("10987654321", Collections.singletonList(
                ArenaVedtakBuilder.create()
                        .dagsats(BigDecimal.valueOf(10000))
                        .tema("DAG")
                        .rettighetType("DAGO")
                        .vedtaksperiode(LocalDate.now(), LocalDate.now().plusDays(1))
                        .build()
        ));
        return mockData;
    }
}
