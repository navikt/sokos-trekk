package no.nav.maskinelletrekk.trekk.behandletrekkvedtak;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.ObjectFactory;
import no.nav.maskinelletrekk.trekk.v1.Trekk;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.TrekkResponse;
import no.nav.maskinelletrekk.trekk.v1.TypeKjoring;
import no.nav.maskinelletrekk.trekk.ytelsevedtak.YtelseVedtakService;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public Trekk behandleTrekkvedtak(Trekk trekk) {
        TypeKjoring typeKjoring = trekk.getTypeKjoring();
        List<TrekkRequest> trekkRequestList = duplikatSjekk(trekk.getTrekkRequest());
        LOGGER.info("Starter behandling av {} trekkvedtak.", trekkRequestList.size());

        TrekkOgPeriode trekkOgPeriode = new TrekkOgPeriode(trekkRequestList);

        Map<String, List<ArenaVedtak>> ytelseskontraktMap = ytelseVedtakService.hentYtelseskontrakt(trekkOgPeriode);

        VedtakBeregning vedtakBeregning = new VedtakBeregning(
                ytelseskontraktMap,
                trekkOgPeriode.getFom(),
                trekkOgPeriode.getTom());

        List<TrekkResponse> trekkResponseList = trekkOgPeriode.getTrekkRequestList().stream()
                .map(vedtakBeregning)
                .collect(Collectors.toList());

        return opprettTrekkResponse(typeKjoring, trekkResponseList);
    }

    private Trekk opprettTrekkResponse(TypeKjoring typeKjoring, List<TrekkResponse> trekkResponseList) {
        Trekk trekk = new ObjectFactory().createTrekk();
        trekk.setTypeKjoring(typeKjoring);
        trekk.getTrekkResponse().addAll(trekkResponseList);
        return trekk;
    }

    private static List<TrekkRequest> duplikatSjekk(List<TrekkRequest> trekkRequestList) {
        Set<String> fnrSet = new HashSet<>();
        for (TrekkRequest trekkRequest : trekkRequestList) {
            if (!fnrSet.contains(trekkRequest.getBruker())) {
                fnrSet.add(trekkRequest.getBruker());
            } else {
                trekkRequestList.remove(trekkRequest);
            }
        }
        return trekkRequestList;
    }

}
