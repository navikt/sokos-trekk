package no.nav.maskinelletrekk.trekk.behandletrekkvedtak;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.Beslutning;
import no.nav.maskinelletrekk.trekk.v1.Periode;
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

public class VedtakBeregning implements Function<TrekkRequestOgPeriode, TrekkResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VedtakBeregning.class);
    public static final int SUM_SCALE = 2;

    private Map<String, List<ArenaVedtak>> arenaVedtakMap;

    VedtakBeregning(Map<String, List<ArenaVedtak>> arenaVedtakMap) {
        this.arenaVedtakMap = arenaVedtakMap;
    }

    @Override
    public TrekkResponse apply(TrekkRequestOgPeriode trekkRequestOgPeriode) {
        TrekkRequest trekkRequest = trekkRequestOgPeriode.getTrekkRequest();

        LOGGER.info("Starter beregning av trekkvedtak[trekkvedtakId:{}, bruker:{}]",
                trekkRequest.getTrekkvedtakId(),
                trekkRequest.getBruker());

        List<ArenaVedtak> arenaVedtakList = finnArenaYtelsesvedtakForBruker(trekkRequestOgPeriode);

        BigDecimal sumArena = kalkulerSumArena(arenaVedtakList);
        BigDecimal sumOs = trekkRequest.getTotalSatsOS();
        int antallVedtakArena = arenaVedtakList.size();
        System system = trekkRequest.getSystem();
        Trekkalternativ trekkalt = trekkRequest.getTrekkalt();

        Beslutning beslutning = beslutt(sumArena, sumOs, system, trekkalt);

        LOGGER.info("Beslutning[trekkVedtakId:{}, bruker:{}]: " +
                        "sumOS:{}, " +
                        "sumArena:{}, " +
                        "antallVedtakArena:{}, " +
                        "beslutning:{}",
                trekkRequest.getTrekkvedtakId(),
                trekkRequest.getBruker(),
                sumOs,
                sumArena,
                antallVedtakArena,
                beslutning);

        return TrekkResponseBuilder.create()
                .trekkvedtakId(trekkRequest.getTrekkvedtakId())
                .totalSatsArena(sumArena)
                .totalSatsOS(sumOs)
                .beslutning(beslutning)
                .system(system)
                .vedtak(arenaVedtakList)
                .build();
    }

    private Beslutning beslutt(BigDecimal sumArena, BigDecimal sumOs, System system, Trekkalternativ trekkalt) {
        Beslutning beslutning;

        if (erLopendeEllerSaldotrekk(trekkalt)) {
            beslutning = besluttLopendeOgSaldotrekk2(sumArena, sumOs, system);
        } else if (erProsenttrekk(trekkalt)) {
            beslutning = besluttProsenttrekk(sumArena, sumOs);
        } else {
            LOGGER.error("Ugyldig trekkalternativ: {}", trekkalt);
            throw new UgyldigTrekkalternativException(String.format("Ugyldig trekkalternativ: %s", trekkalt));
        }

        return beslutning;
    }

    private Beslutning besluttLopendeOgSaldotrekk2(BigDecimal sumArena, BigDecimal sumOs, System system) {
        Beslutning beslutning;
        if (erAbetal(system) && sumArena.compareTo(sumOs) >= 0 && sumArena.compareTo(ZERO) != 0
                || sumArena.compareTo(sumOs) >= 0 && sumArena.compareTo(ZERO) > 0) {
            beslutning = ABETAL;
        } else if (sumOs.compareTo(sumArena) > 0) {
            beslutning = OS;
        } else {
            beslutning = INGEN;
        }
        return beslutning;
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

    private boolean erAbetal(System system) {
        return J.equals(system);
    }

    private boolean erProsenttrekk(Trekkalternativ trekkalt) {
        return trekkalt == Trekkalternativ.LOPP
                || trekkalt == Trekkalternativ.SALP;
    }

    private boolean erLopendeEllerSaldotrekk(Trekkalternativ trekkalt) {
        return trekkalt == Trekkalternativ.LOPD
                || trekkalt == Trekkalternativ.LOPM
                || trekkalt == Trekkalternativ.SALD
                || trekkalt == Trekkalternativ.SALM;
    }

    private List<ArenaVedtak> finnArenaYtelsesvedtakForBruker(TrekkRequestOgPeriode trekkRequestOgPeriode) {
        List<ArenaVedtak> arenaVedtakList = new ArrayList<>();

        int trekkvedtakId = trekkRequestOgPeriode.getTrekkRequest().getTrekkvedtakId();
        String bruker = trekkRequestOgPeriode.getTrekkRequest().getBruker();

        if (arenaVedtakMap.containsKey(bruker)) {
            for (ArenaVedtak arenaVedtak : arenaVedtakMap.get(bruker)) {
                Periode requestPeriode = new Periode();
                requestPeriode.setFom(trekkRequestOgPeriode.getFom());
                requestPeriode.setTom(trekkRequestOgPeriode.getTom());
                if (PeriodeSjekk.erInnenforPeriode(arenaVedtak.getVedtaksperiode(), requestPeriode)) {
                    arenaVedtakList.add(arenaVedtak);
                }
            }
        }
        LOGGER.info("Funnet {} Arena-vedtak for trekkvedtak[trekkvedtakId: {}, bruker {}]",
                arenaVedtakList.size(), trekkvedtakId, bruker);
        return arenaVedtakList;
    }

    private BigDecimal kalkulerSumArena(List<ArenaVedtak> arenaVedtakList) {
        return arenaVedtakList.stream()
                .map(ArenaVedtak::getDagsats)
                .reduce(ZERO, BigDecimal::add)
                .setScale(SUM_SCALE, ROUND_HALF_UP);
    }

}
