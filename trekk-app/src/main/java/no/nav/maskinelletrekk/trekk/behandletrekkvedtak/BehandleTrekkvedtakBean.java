package no.nav.maskinelletrekk.trekk.behandletrekkvedtak;

import io.micrometer.core.annotation.Timed;
import no.nav.maskinelletrekk.trekk.config.Metrikker;
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

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Component
public class BehandleTrekkvedtakBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(BehandleTrekkvedtakBean.class);

    private final YtelseVedtakService ytelseVedtakService;
    private final Clock clock;

    @Autowired
    public BehandleTrekkvedtakBean(YtelseVedtakService ytelseVedtakService, Clock clock) {
        this.ytelseVedtakService = requireNonNull(ytelseVedtakService, "ytelseVedtakService must not be null");
        this.clock = requireNonNull(clock, "clock must not be null");
    }

    @Handler
    @Timed(Metrikker.VEDTAK_BEREGNING_TIMER)
    public Trekk behandleTrekkvedtak(Trekk trekk) {
        TypeKjoring typeKjoring = trekk.getTypeKjoring();
        List<TrekkRequest> trekkRequestList = duplikatTrekkvedtakIdSjekk(trekk.getTrekkRequest());
        LOGGER.info("Starter behandling av {} trekkvedtak.", trekkRequestList.size());

        Map<String, List<ArenaVedtak>> ytelseskontraktMap = kallHentYtelseskontrakt(trekkRequestList);

        VedtakBeregning vedtakBeregning = new VedtakBeregning(ytelseskontraktMap);
        List<TrekkResponse> trekkResponseList = trekkRequestList.stream()
                .map(vedtakBeregning)
                .collect(Collectors.toList());

        return opprettTrekkResponse(typeKjoring, trekkResponseList);
    }

    private Map<String, List<ArenaVedtak>> kallHentYtelseskontrakt(List<TrekkRequest> trekkRequestList) {
        Set<String> brukerSet = trekkRequestList.stream().map(TrekkRequest::getOffnr).collect(Collectors.toSet());

        YearMonth nextMonth = YearMonth.now(clock).plusMonths(1);
        LocalDate fom = LocalDate.now(clock);
        LocalDate tom = nextMonth.atEndOfMonth();

        return ytelseVedtakService.hentYtelseskontrakt(brukerSet, fom, tom);
    }

    private Trekk opprettTrekkResponse(TypeKjoring typeKjoring, List<TrekkResponse> trekkResponseList) {
        Trekk trekk = new ObjectFactory().createTrekk();
        trekk.setTypeKjoring(typeKjoring);
        trekk.getTrekkResponse().addAll(trekkResponseList);
        return trekk;
    }

    private static List<TrekkRequest> duplikatTrekkvedtakIdSjekk(List<TrekkRequest> trekkRequestList) {
        List<TrekkRequest> newList = new ArrayList<>();
        Set<Integer> trekkvedtakIdSet = new HashSet<>();
        for (TrekkRequest trekkRequest : trekkRequestList) {
            if (!trekkvedtakIdSet.contains(trekkRequest.getTrekkvedtakId())) {
                trekkvedtakIdSet.add(trekkRequest.getTrekkvedtakId());
                newList.add(trekkRequest);
            }
        }
        return newList;
    }

}
