package no.nav.maskinelletrekk.behandletrekkvedtak;

import no.nav.maskinelletrekk.trekk.v1.ObjectFactory;
import no.nav.maskinelletrekk.trekk.v1.Oppdragsvedtak;
import no.nav.maskinelletrekk.trekk.v1.Trekk;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.TrekkResponse;
import no.nav.maskinelletrekk.trekk.v1.Vedtak;
import no.nav.maskinelletrekk.ytelsevedtak.YtelseVedtakService;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

        Map<TrekkRequest, TrekkResponse> responseMap = ytelseVedtakService.hentYtelseskontrakt(trekkRequestList);

        LOGGER.info("Kalkulerer summer for trekkVedtak");
        Trekk trekkResponse = opprettResponse();
        responseMap.forEach((request, response) -> {

            BigDecimal sumOs = request.getOppdragsvedtak().stream().map(Oppdragsvedtak::getSats).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumArena = response.getVedtak().stream().map(Vedtak::getDagsats).reduce(BigDecimal.ZERO, BigDecimal::add);
            LOGGER.info("sum OS: {}", sumOs);
            LOGGER.info("sum Arena: {}", sumArena);
            trekkResponse.getTrekkResponse().add(response);
            response.setTotalSatsArena(sumArena);
            response.setTotalSatsOS(sumOs);
            response.setTrekkvedtakId(request.getTrekkvedtakId());
        });
        return trekkResponse;
    }

    private Trekk opprettResponse() {
        return new ObjectFactory().createTrekk();
    }

}
