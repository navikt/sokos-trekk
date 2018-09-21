package no.nav.maskinelletrekk.trekk.v1.builder;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.Beslutning;
import no.nav.maskinelletrekk.trekk.v1.OsParams;
import no.nav.maskinelletrekk.trekk.v1.System;
import no.nav.maskinelletrekk.trekk.v1.TrekkResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public final class TrekkResponseBuilder {

    private int trekkvedtakId;
    private Beslutning beslutning;
    private System system;
    private BigDecimal totalSatsOS;
    private BigDecimal totalSatsArena;
    private String msgId;
    private String partnerRef;
    private String ediLoggId;
    private List<ArenaVedtak> vedtak = new ArrayList<>();

    private TrekkResponseBuilder() {
    }

    public static TrekkResponseBuilder create() {
        return new TrekkResponseBuilder();
    }

    public TrekkResponseBuilder trekkvedtakId(int trekkvedtakId) {
        this.trekkvedtakId = trekkvedtakId;
        return this;
    }

    public TrekkResponseBuilder beslutning(Beslutning beslutning) {
        this.beslutning = beslutning;
        return this;
    }

    public TrekkResponseBuilder system(System system) {
        this.system = system;
        return this;
    }

    public TrekkResponseBuilder totalSatsOS(BigDecimal totalSatsOS) {
        this.totalSatsOS = totalSatsOS;
        return this;
    }

    public TrekkResponseBuilder totalSatsArena(BigDecimal totalSatsArena) {
        this.totalSatsArena = totalSatsArena;
        return this;
    }


    public TrekkResponseBuilder msgId(String msgId) {
        this.msgId = msgId;
        return this;
    }

    public TrekkResponseBuilder partnerRef(String partnerRef) {
        this.partnerRef = partnerRef;
        return this;
    }

    public TrekkResponseBuilder ediLoggId(String ediLoggId) {
        this.ediLoggId = ediLoggId;
        return this;
    }

    public TrekkResponseBuilder vedtak(List<ArenaVedtak> vedtak) {
        this.vedtak = vedtak;
        return this;
    }

    public TrekkResponseBuilder vedtak(ArenaVedtak... vedtak) {
        this.vedtak.addAll(Arrays.asList(vedtak));
        return this;
    }

    public TrekkResponse build() {
        TrekkResponse trekkResponse = new TrekkResponse();
        trekkResponse.setTrekkvedtakId(trekkvedtakId);
        trekkResponse.setBeslutning(beslutning);
        trekkResponse.setSystem(system);
        trekkResponse.setTotalSatsOS(totalSatsOS);
        trekkResponse.setTotalSatsArena(totalSatsArena);
        if (isNotEmpty(msgId)
                || isNotEmpty(partnerRef)
                || isNotEmpty(ediLoggId)) {
            OsParams osParams = new OsParams();
            osParams.setPartnerRef(partnerRef);
            osParams.setEdiLoggId(ediLoggId);
            osParams.setMsgId(msgId);
            trekkResponse.setOsParams(osParams);
        }
        trekkResponse.getVedtak().addAll(vedtak);
        return trekkResponse;
    }
}
