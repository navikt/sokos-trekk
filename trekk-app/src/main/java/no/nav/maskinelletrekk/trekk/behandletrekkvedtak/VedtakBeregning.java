package no.nav.maskinelletrekk.trekk.behandletrekkvedtak;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.Beslutning;
import no.nav.maskinelletrekk.trekk.v1.System;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.TrekkResponse;
import no.nav.maskinelletrekk.trekk.v1.Trekkalternativ;
import no.nav.maskinelletrekk.trekk.v1.builder.TrekkResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.math.BigDecimal.ZERO;
import static no.nav.maskinelletrekk.trekk.v1.Beslutning.ABETAL;
import static no.nav.maskinelletrekk.trekk.v1.Beslutning.BEGGE;
import static no.nav.maskinelletrekk.trekk.v1.Beslutning.INGEN;
import static no.nav.maskinelletrekk.trekk.v1.Beslutning.OS;
import static no.nav.maskinelletrekk.trekk.v1.System.J;

public class VedtakBeregning implements Function<TrekkRequest, TrekkResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VedtakBeregning.class);
    public static final int SUM_SCALE = 2;

    private Map<String, List<ArenaVedtak>> arenaVedtakMap;

    VedtakBeregning(Map<String, List<ArenaVedtak>> arenaVedtakMap) {
        this.arenaVedtakMap = arenaVedtakMap;
    }

    @Override
    public TrekkResponse apply(TrekkRequest trekkRequest) {
        int trekkvedtakId = trekkRequest.getTrekkvedtakId();

        List<ArenaVedtak> arenaVedtakList = finnArenaYtelsesvedtakForBruker(trekkRequest);

        int antallVedtakArena = arenaVedtakList.size();
        BigDecimal sumArena = kalkulerSumArena(arenaVedtakList);
        BigDecimal sumOs = trekkRequest.getTotalSatsOS();
        System system = trekkRequest.getSystem();
        BigDecimal trekkSats = trekkRequest.getTrekkSats();
        Trekkalternativ trekkalt = trekkRequest.getTrekkalt();

        LOGGER.info("Starter beregning av trekkvedtak[trekkvedtakId:{}]", trekkvedtakId);

        Beslutning beslutning = beslutt(sumArena, sumOs, trekkSats, system, trekkalt);

        LOGGER.info("Beslutning[trekkVedtakId:{}]: " +
                        "sumOS:{}, " +
                        "sumArena:{}, " +
                        "antallVedtakArena:{}, " +
                        "beslutning:{}",
                trekkvedtakId,
                sumOs,
                sumArena,
                antallVedtakArena,
                beslutning);

        return TrekkResponseBuilder.create()
                .trekkvedtakId(trekkvedtakId)
                .totalSatsArena(sumArena)
                .totalSatsOS(sumOs)
                .beslutning(beslutning)
                .system(system)
                .vedtak(arenaVedtakList).build();
    }

    private Beslutning beslutt(BigDecimal sumArena, BigDecimal sumOs, BigDecimal trekkSats, System system, Trekkalternativ trekkalt) {
        switch (trekkalt) {
            case SALP:
            case LOPP:
                return besluttProsenttrekk(sumArena, sumOs);
            case LOPD:
            case LOPM:
            case SALD:
            case SALM:
            default:
                return besluttLopendeOgSaldotrekk(sumArena, sumOs, trekkSats, system);
        }
    }

    private Beslutning besluttProsenttrekk(BigDecimal sumArena, BigDecimal sumOs) {
        Beslutning beslutning = null;
        if (sumArena.compareTo(ZERO) > 0) {
            beslutning = ABETAL;
        }
        if (sumOs.compareTo(ZERO) > 0) {
            if (beslutning == ABETAL) {
                beslutning = BEGGE;
            } else {
                beslutning = OS;
            }
        } else if (beslutning != ABETAL) {
            beslutning = INGEN;
        }
        return beslutning;
    }

    private Beslutning besluttLopendeOgSaldotrekk(BigDecimal sumArena, BigDecimal sumOs, BigDecimal trekkSats, System system) {
        Beslutning beslutning;
        if (erAbetal(system) && sumArena.compareTo(trekkSats) >= 0 && sumArena.compareTo(ZERO) != 0
                || sumArena.compareTo(sumOs) >= 0 && sumArena.compareTo(ZERO) > 0) {
            beslutning = ABETAL;
        } else if (sumOs.compareTo(sumArena) > 0) {
            beslutning = OS;
        } else {
            beslutning = INGEN;
        }
        return beslutning;
    }

    private boolean erAbetal(System system) {
        return J.equals(system);
    }

    private List<ArenaVedtak> finnArenaYtelsesvedtakForBruker(TrekkRequest trekkRequest) {
        List<ArenaVedtak> arenaVedtakList = new ArrayList<>();

        String bruker = trekkRequest.getOffnr();

        if (arenaVedtakMap.containsKey(bruker)) {
            arenaVedtakList.addAll(arenaVedtakMap.get(bruker));
        }
        LOGGER.info("Funnet {} Arena-vedtak for trekkvedtak[trekkvedtakId: {}]",
                arenaVedtakList.size(), trekkRequest.getTrekkvedtakId());
        return arenaVedtakList;
    }

    private BigDecimal kalkulerSumArena(List<ArenaVedtak> arenaVedtakList) {
        return arenaVedtakList.stream()
                .map(ArenaVedtak::getDagsats)
                .reduce(ZERO, BigDecimal::add)
                .setScale(SUM_SCALE, ROUND_HALF_UP);
    }

}
