package no.nav.maskinelletrekk;

import no.nav.maskinelletrekk.trekk.v1.ObjectFactory;
import no.nav.maskinelletrekk.trekk.v1.Trekk;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequestListe;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class BehandleTrekkvedtakBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(BehandleTrekkvedtakBean.class);

    private YtelseskontraktService ytelseskontraktService;

    @Autowired
    public BehandleTrekkvedtakBean(YtelseskontraktService ytelseskontraktService) {
        Assert.notNull(ytelseskontraktService, "ytelseskontraktService must not be null");
        this.ytelseskontraktService = ytelseskontraktService;
    }

    @Handler
    public Trekk marshal(Trekk request) {
        TrekkRequestListe trekkRequestListe = request.getTrekkRequestListe();
        LOGGER.info("Behandler trekkvedtak med ID {}", trekkRequestListe.getTrekkRequest().get(0).getTrekkvedtakId());
        return new ObjectFactory().createTrekk();
    }

}
