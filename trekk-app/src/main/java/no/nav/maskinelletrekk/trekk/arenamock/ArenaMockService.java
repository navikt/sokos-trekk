package no.nav.maskinelletrekk.trekk.arenamock;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.ytelsevedtak.YtelseVedtakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ArenaMockService implements YtelseVedtakService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArenaMockService.class);

    private Map<String, List<ArenaVedtak>> mockDataMap;

    @Override
    public Map<String, List<ArenaVedtak>> hentYtelseskontrakt(List<TrekkRequest> trekkRequestListe){
        Set<String> fnrList = trekkRequestListe.stream().map(TrekkRequest::getBruker).collect(Collectors.toSet());

        Map<String, List<ArenaVedtak>> vedtakMap = new HashMap<>();

        for (String s : fnrList) {
            if (mockDataMap.containsKey(s)) {
                vedtakMap.put(s, mockDataMap.get(s));
            }
        }
        return vedtakMap;
    }

    void setMockDataMap(Map<String, List<ArenaVedtak>> mockDataMap) {
        this.mockDataMap = mockDataMap;
    }
}
