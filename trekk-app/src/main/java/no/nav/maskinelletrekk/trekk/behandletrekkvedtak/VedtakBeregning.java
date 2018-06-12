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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.math.BigDecimal.ROUND_HALF_UP;

public class VedtakBeregning implements Function<TrekkRequestOgPeriode, TrekkResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VedtakBeregning.class);

    public static final int SUM_SCALE = 2;
    private static final double MND_FAKTOR = 21.67;

    private Map<String, List<ArenaVedtak>> ytelseskontraktMap;

    VedtakBeregning(Map<String, List<ArenaVedtak>> ytelseskontraktMap) {
        this.ytelseskontraktMap = ytelseskontraktMap;
    }

    @Override
    public TrekkResponse apply(TrekkRequestOgPeriode trekkRequestOgPeriode) {
        TrekkRequest request = trekkRequestOgPeriode.getTrekkRequest();
        List<Oppdragsvedtak> oppdragVedtakList = request.getOppdragsvedtak();

        LOGGER.info("Beregner trekkvedtak: trekkvedtakId:{}, bruker:{}",
                request.getTrekkvedtakId(),
                request.getBruker());
        List<ArenaVedtak> arenaVedtakList = finnYtelsesvedtakForBruker(trekkRequestOgPeriode);

        BigDecimal sumArena = kalkulerSumArena(arenaVedtakList);
        BigDecimal sumOs = kalkulerSumOppdrag(oppdragVedtakList);
        int antallVedtakOS = oppdragVedtakList.size();
        int antallVedtakArena = arenaVedtakList.size();
        Beslutning besluttning = beslutt(sumArena, sumOs, antallVedtakArena, antallVedtakOS);

        LOGGER.info("Beslutning[trekkVedtakId:{}, bruker:{}]: " +
                        "sumOS:{}, " +
                        "antallvedtakOS:{}, " +
                        "sumArena:{}, " +
                        "antallVedtakArena:{}, " +
                        "beslutning:{}",
                request.getTrekkvedtakId(),
                request.getBruker(),
                sumOs,
                antallVedtakOS,
                sumArena,
                antallVedtakArena,
                besluttning);

        return TrekkResponseBuilder.create()
                .trekkvedtakId(request.getTrekkvedtakId())
                .totalSatsArena(sumArena)
                .totalSatsOS(sumOs)
                .beslutning(besluttning)
                .vedtak(arenaVedtakList)
                .build();
    }

    private List<ArenaVedtak> finnYtelsesvedtakForBruker(TrekkRequestOgPeriode trekkRequest) {
        List<ArenaVedtak> arenaVedtakList = new ArrayList<>();
        String bruker = trekkRequest.getTrekkRequest().getBruker();
        LOGGER.info("Finner vedtak for bruker {}", bruker);
        if (ytelseskontraktMap.containsKey(bruker)) {
            for (ArenaVedtak arenaVedtak : ytelseskontraktMap.get(bruker)) {
                Periode requestPeriode = new Periode();
                requestPeriode.setFom(trekkRequest.getFom());
                requestPeriode.setTom(trekkRequest.getTom());
                if (erInnenforPeriode(arenaVedtak.getVedtaksperiode(), requestPeriode)) {
                    arenaVedtakList.add(arenaVedtak);
                }
            }
        }
        return arenaVedtakList;
    }

    private boolean erInnenforPeriode(Periode arenaVedtaksPeriode, Periode requestPeriode) {
        return arenaVedtaksPeriode.getTom() == null && arenaVedtaksPeriode.getFom().isBefore(requestPeriode.getTom())
                || erVedtakPeriodeStorreEnnRequestPeriode(arenaVedtaksPeriode.getFom(), arenaVedtaksPeriode.getTom(), requestPeriode)
                || erVedtakDatoInnenforRequestPeriode(arenaVedtaksPeriode.getTom(), requestPeriode)
                || erVedtakDatoInnenforRequestPeriode(arenaVedtaksPeriode.getFom(), requestPeriode);
    }

    private boolean erVedtakDatoInnenforRequestPeriode(LocalDate dato, Periode requestPeriode) {
        return (dato.isAfter(requestPeriode.getFom()) || dato.isEqual(requestPeriode.getFom()))
                && (dato.isBefore(requestPeriode.getTom()) || dato.isEqual(requestPeriode.getTom()));
    }

    private boolean erVedtakPeriodeStorreEnnRequestPeriode(LocalDate fom, LocalDate tom, Periode requestPeriode) {
        return fom.isBefore(requestPeriode.getFom())
                && (tom.isAfter(requestPeriode.getFom()));
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
