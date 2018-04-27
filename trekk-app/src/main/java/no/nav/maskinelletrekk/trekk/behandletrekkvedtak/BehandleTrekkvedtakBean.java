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
        List<TrekkRequest> trekkRequestList = trekkRequest.getTrekkRequest();
        LOGGER.info("Starter behandling av {} trekkvedtak.", trekkRequestList.size());

        Map<String, List<ArenaVedtak>> ytelseskontraktMap = ytelseVedtakService.hentYtelseskontrakt(trekkRequestList);

        VedtakBeregning vedtakBeregning = new VedtakBeregning(ytelseskontraktMap);
        List<TrekkResponse> trekkResponseList = trekkRequestList.stream()
                .map(vedtakBeregning)
                .collect(Collectors.toList());

        return opprettTrekkResponse(trekkResponseList);
    }

    private Trekk opprettTrekkResponse(List<TrekkResponse> trekkResponseList) {
        Trekk trekk = new ObjectFactory().createTrekk();
        trekk.getTrekkResponse().addAll(trekkResponseList);
        return trekk;
    }

}
