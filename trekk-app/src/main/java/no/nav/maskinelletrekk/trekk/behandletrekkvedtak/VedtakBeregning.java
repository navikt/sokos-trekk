package no.nav.maskinelletrekk.trekk.behandletrekkvedtak;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.Beslutning;
import no.nav.maskinelletrekk.trekk.v1.Oppdragsvedtak;
import no.nav.maskinelletrekk.trekk.v1.Periode;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.TrekkResponse;
import no.nav.maskinelletrekk.trekk.v1.TypeSats;
import no.nav.maskinelletrekk.trekk.v1.builder.TrekkResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.math.BigDecimal.ROUND_HALF_UP;

public class VedtakBeregning implements Function<TrekkRequestOgPeriode, TrekkResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VedtakBeregning.class);

    public static final int SUM_SCALE = 2;
    private static final double MND_FAKTOR = 21.67;

    private Map<String, List<ArenaVedtak>> arenaVedtakMap;

    VedtakBeregning(Map<String, List<ArenaVedtak>> arenaVedtakMap) {
        this.arenaVedtakMap = arenaVedtakMap;
    }

    @Override
    public TrekkResponse apply(TrekkRequestOgPeriode trekkRequestOgPeriode) {
        TrekkRequest trekkRequest = trekkRequestOgPeriode.getTrekkRequest();
        List<Oppdragsvedtak> oppdragVedtakList = trekkRequest.getOppdragsvedtak();

        LOGGER.info("Starter beregning av trekkvedtak[trekkvedtakId:{}, bruker:{}]",
                trekkRequest.getTrekkvedtakId(),
                trekkRequest.getBruker());

        List<ArenaVedtak> arenaVedtakList = finnArenaYtelsesvedtakForBruker(trekkRequestOgPeriode);

        BigDecimal sumArena = kalkulerSumArena(arenaVedtakList);
        BigDecimal sumOs = kalkulerSumOppdrag(oppdragVedtakList);
        int antallVedtakOS = oppdragVedtakList.size();
        int antallVedtakArena = arenaVedtakList.size();
        Beslutning beslutning = beslutt(sumArena, sumOs, antallVedtakArena, antallVedtakOS);

        LOGGER.info("Beslutning[trekkVedtakId:{}, bruker:{}]: " +
                        "sumOS:{}, " +
                        "antallvedtakOS:{}, " +
                        "sumArena:{}, " +
                        "antallVedtakArena:{}, " +
                        "beslutning:{}",
                trekkRequest.getTrekkvedtakId(),
                trekkRequest.getBruker(),
                sumOs,
                antallVedtakOS,
                sumArena,
                antallVedtakArena,
                beslutning);

        return TrekkResponseBuilder.create()
                .trekkvedtakId(trekkRequest.getTrekkvedtakId())
                .totalSatsArena(sumArena)
                .totalSatsOS(sumOs)
                .beslutning(beslutning)
                .vedtak(arenaVedtakList)
                .build();
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

    private Beslutning beslutt(BigDecimal sumArena, BigDecimal sumOs, int numArena, int numOs) {
        Beslutning beslutning;
        if (numArena == 0 && numOs == 0) {
            beslutning = Beslutning.INGEN;
        } else {
            beslutning = sumArena.compareTo(sumOs) > 0 ? Beslutning.ABETAL : Beslutning.OS;
        }
        return beslutning;
    }

    private BigDecimal kalkulerSumOppdrag(List<Oppdragsvedtak> oppdragsvedtakList) {
        return oppdragsvedtakList.stream()
                .map(oppdragsvedtak -> {
                    BigDecimal sats = oppdragsvedtak.getSats();
                    if (TypeSats.MND.equals(oppdragsvedtak.getTypeSats())) {
                        sats = sats.divide(BigDecimal.valueOf(MND_FAKTOR), SUM_SCALE, ROUND_HALF_UP);
                    }
                    return sats;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SUM_SCALE, ROUND_HALF_UP);
    }

    private BigDecimal kalkulerSumArena(List<ArenaVedtak> arenaVedtakList) {
        return arenaVedtakList.stream()
                .map(ArenaVedtak::getDagsats)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SUM_SCALE, ROUND_HALF_UP);
    }

}
