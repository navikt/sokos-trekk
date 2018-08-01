package no.nav.maskinelletrekk.trekk.behandletrekkvedtak;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.ObjectFactory;
import no.nav.maskinelletrekk.trekk.v1.Trekk;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.TrekkResponse;
import no.nav.maskinelletrekk.trekk.ytelsevedtak.YtelseVedtakService;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BehandleTrekkvedtakBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(BehandleTrekkvedtakBean.class);

    private YtelseVedtakService ytelseVedtakService;

    @Autowired
    public BehandleTrekkvedtakBean(YtelseVedtakService ytelseVedtakService) {
        Assert.notNull(ytelseVedtakService, "ytelseVedtakService must not be null");
        this.ytelseVedtakService = ytelseVedtakService;
    }

    @Handler
    public Trekk behandleTrekkvedtak(Trekk trekkRequest) {
        String typeKjoring = trekkRequest.getTypeKjoring();
        List<TrekkRequest> trekkRequestList = trekkRequest.getTrekkRequest();
        LOGGER.info("Starter behandling av {} trekkvedtak.", trekkRequestList.size());

        List<TrekkRequestOgPeriode> trekkRequestOgPeriodeList =
                fjernDuplikaterOgAggregerPerioder(trekkRequestList.stream()
                        .map(TrekkRequestOgPeriode::new)
                        .collect(Collectors.toList()));
        Map<String, List<ArenaVedtak>> ytelseskontraktMap = ytelseVedtakService.hentYtelseskontrakt(trekkRequestOgPeriodeList);

        VedtakBeregning vedtakBeregning = new VedtakBeregning(ytelseskontraktMap);
        List<TrekkResponse> trekkResponseList = trekkRequestOgPeriodeList.stream()
                .map(vedtakBeregning)
                .collect(Collectors.toList());

        return opprettTrekkResponse(typeKjoring, trekkResponseList);
    }

    private Trekk opprettTrekkResponse(String typeKjoring, List<TrekkResponse> trekkResponseList) {
        Trekk trekk = new ObjectFactory().createTrekk();
        trekk.setTypeKjoring(typeKjoring);
        trekk.getTrekkResponse().addAll(trekkResponseList);
        return trekk;
    }

    private static List<TrekkRequestOgPeriode> fjernDuplikaterOgAggregerPerioder(List<TrekkRequestOgPeriode> trekkRequestListe) {
        Map<String, TrekkRequestOgPeriode> trekkRequestOgPeriodeMap = new HashMap<>();
        for (TrekkRequestOgPeriode nyRequest : trekkRequestListe) {
            String fnr = nyRequest.getTrekkRequest().getBruker();
            if (trekkRequestOgPeriodeMap.containsKey(fnr)) {
                TrekkRequestOgPeriode eksisterendeRequest = trekkRequestOgPeriodeMap.get(fnr);
                if (nyRequest.getFom().isBefore(eksisterendeRequest.getFom())) {
                    eksisterendeRequest.setFom(nyRequest.getFom());
                }
                if (nyRequest.getTom().isAfter(eksisterendeRequest.getTom())) {
                    eksisterendeRequest.setTom(nyRequest.getTom());
                }
            } else {
                trekkRequestOgPeriodeMap.put(fnr, nyRequest);
            }
        }
        return new ArrayList<>(trekkRequestOgPeriodeMap.values());
    }

}
