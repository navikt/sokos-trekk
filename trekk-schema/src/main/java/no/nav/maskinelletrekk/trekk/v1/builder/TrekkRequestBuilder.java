package no.nav.maskinelletrekk.trekk.v1.builder;

import no.nav.maskinelletrekk.trekk.v1.ObjectFactory;
import no.nav.maskinelletrekk.trekk.v1.OsParams;
import no.nav.maskinelletrekk.trekk.v1.System;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.Trekkalternativ;

import java.math.BigDecimal;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public final class TrekkRequestBuilder {

    private String offnr;
    private int trekkvedtakId;
    private Trekkalternativ trekkalt;
    private System system;
    private BigDecimal totalSatsOS;
    private String msgId;
    private String partnerRef;
    private String ediLoggId;

    private TrekkRequestBuilder() {
    }

    public static TrekkRequestBuilder create() {
        return new TrekkRequestBuilder();
    }

    public TrekkRequestBuilder offnr(String offnr) {
        this.offnr = offnr;
        return this;
    }

    public TrekkRequestBuilder trekkvedtakId(int trekkvedtakId) {
        this.trekkvedtakId = trekkvedtakId;
        return this;
    }

    public TrekkRequestBuilder trekkalt(Trekkalternativ trekkalt) {
        this.trekkalt = trekkalt;
        return this;
    }

    public TrekkRequestBuilder system(System system) {
        this.system = system;
        return this;
    }
    public TrekkRequestBuilder totalSatsOS(BigDecimal totalSatsOS) {
        this.totalSatsOS = totalSatsOS;
        return this;
    }

    public TrekkRequestBuilder msgId(String msgId) {
        this.msgId = msgId;
        return this;
    }

    public TrekkRequestBuilder partnerRef(String partnerRef) {
        this.partnerRef = partnerRef;
        return this;
    }

    public TrekkRequestBuilder ediLoggId(String ediLoggId) {
        this.ediLoggId = ediLoggId;
        return this;
    }

    public TrekkRequest build() {
        TrekkRequest trekkRequest = new ObjectFactory().createTrekkRequest();
        trekkRequest.setOffnr(offnr);
        trekkRequest.setTrekkvedtakId(trekkvedtakId);
        trekkRequest.setTrekkalt(trekkalt);
        trekkRequest.setSystem(system);
        trekkRequest.setTotalSatsOS(totalSatsOS);
        if (isNotEmpty(msgId)
                || isNotEmpty(partnerRef)
                || isNotEmpty(ediLoggId)) {
            OsParams osParams = new OsParams();
            osParams.setMsgId(msgId);
            osParams.setPartnerRef(partnerRef);
            osParams.setEdiLoggId(ediLoggId);
            trekkRequest.setOsParams(osParams);
        }
        return trekkRequest;
    }

}
