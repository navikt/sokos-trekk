package no.nav.maskinelletrekk.trekk.behandletrekkvedtak;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.Beslutning;
import no.nav.maskinelletrekk.trekk.v1.System;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.TrekkResponse;
import no.nav.maskinelletrekk.trekk.v1.Trekkalternativ;
import no.nav.maskinelletrekk.trekk.v1.builder.ArenaVedtakBuilder;
import no.nav.maskinelletrekk.trekk.v1.builder.TrekkRequestBuilder;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class VedtakBeregningTest {

    private static final String FNR_1 = "12312312312";
    private static final BigDecimal DAGSATS_1 = new BigDecimal("123.00");
    private static final BigDecimal DAGSATS_2 = new BigDecimal("123.00");
    private static final BigDecimal DAGSATS_3 = new BigDecimal("123.00");
    private static final LocalDate NOW = LocalDate.now();
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Test
    public void besluttAbetal_TypeLopendeTrekk_AktivAbetal_YtelseIAbetalErTilstrekkelig() {
        TrekkRequest request = opprettTrekkRequest(Trekkalternativ.LOPD, System.J, DAGSATS_1);
        VedtakBeregning beregning = new VedtakBeregning(opprettArenaYtelser(FNR_1, asList(
                ArenaVedtakBuilder.create()
                        .dagsats(DAGSATS_1)
                        .vedtaksperiode(LocalDate.now().minusDays(10), null)
                        .build(),
                ArenaVedtakBuilder.create()
                        .dagsats(DAGSATS_2)
                        .vedtaksperiode(LocalDate.now(), LocalDate.now().plusDays(30))
                        .build()
        )));

        TrekkResponse response = beregning.apply(request);

        assertThat(response.getTrekkvedtakId(), equalTo(1));
        assertThat(response.getSystem(), equalTo(System.J));
        assertThat(response.getBeslutning(), equalTo(Beslutning.ABETAL));
        assertThat(response.getVedtak().size(), equalTo(2));

    }

    @Test
    public void besluttOS_TypeLopendeTrekk_AktivAbetal_YtelseAbetalErIkkeTilstrekkelig() {

        TrekkRequest request = opprettTrekkRequest(Trekkalternativ.LOPD, System.J, DAGSATS_2.add(BigDecimal.ONE));
        VedtakBeregning beregning = new VedtakBeregning(opprettArenaYtelser(FNR_1, singletonList(
                ArenaVedtakBuilder.create()
                        .dagsats(DAGSATS_2)
                        .vedtaksperiode(LocalDate.now(), LocalDate.now().plusDays(30))
                        .build()
        )));
        TrekkResponse response = beregning.apply(request);

        assertThat(response.getTrekkvedtakId(), equalTo(1));
        assertThat(response.getBeslutning(), equalTo(Beslutning.OS));
        assertThat(response.getSystem(), equalTo(System.J));
        assertThat(response.getTotalSatsArena(), equalTo(DAGSATS_2));
        assertThat(response.getTotalSatsOS(), equalTo(DAGSATS_2.add(BigDecimal.ONE)));
        assertThat(response.getVedtak().size(), equalTo(1));
    }

    @Test
    public void besluttOS_TypeLopendeTrekk_IkkeAktivtAbetal_YtelseIOSErTilstrekkelig() {
        TrekkRequest request = opprettTrekkRequest(Trekkalternativ.LOPD, System.N, DAGSATS_2.add(BigDecimal.ONE));
        VedtakBeregning beregning = new VedtakBeregning(opprettArenaYtelser(FNR_1, singletonList(
                ArenaVedtakBuilder.create()
                        .dagsats(DAGSATS_2)
                        .vedtaksperiode(LocalDate.now(), LocalDate.now().plusDays(30))
                        .build()
        )));
        TrekkResponse response = beregning.apply(request);

        assertThat(response.getTrekkvedtakId(), equalTo(1));
        assertThat(response.getBeslutning(), equalTo(Beslutning.OS));
        assertThat(response.getSystem(), equalTo(System.N));
        assertThat(response.getTotalSatsArena(), equalTo(DAGSATS_2));
        assertThat(response.getTotalSatsOS(), equalTo(DAGSATS_2.add(BigDecimal.ONE)));
        assertThat(response.getVedtak().size(), equalTo(1));
    }

    @Test
    public void besluttAbetal_TypeLopendeTrekk_IkkeAktivAbetal_YtelseOsErIkkeTilstrekkelig_YtelseAbetalErStorreEnnNull() {
        TrekkRequest request = opprettTrekkRequest(Trekkalternativ.LOPD, System.N, DAGSATS_2.subtract(BigDecimal.ONE));
        VedtakBeregning beregning = new VedtakBeregning(opprettArenaYtelser(FNR_1, singletonList(
                ArenaVedtakBuilder.create()
                        .dagsats(DAGSATS_2)
                        .vedtaksperiode(LocalDate.now(), LocalDate.now().plusDays(30))
                        .build()
        )));
        TrekkResponse response = beregning.apply(request);

        assertThat(response.getTrekkvedtakId(), equalTo(1));
        assertThat(response.getBeslutning(), equalTo(Beslutning.ABETAL));
        assertThat(response.getSystem(), equalTo(System.N));
        assertThat(response.getTotalSatsArena(), equalTo(DAGSATS_2));
        assertThat(response.getTotalSatsOS(), equalTo(DAGSATS_2.subtract(BigDecimal.ONE)));
        assertThat(response.getVedtak().size(), equalTo(1));
    }

    @Test
    public void besluttIngen_TypeLopende_IkkeAktivtIAbetal_YtelseOsIkkeTilstrekkelig_YtelseAbetalErNull() {
        TrekkRequest request = opprettTrekkRequest(Trekkalternativ.LOPD, System.N, ZERO);
        VedtakBeregning beregning = new VedtakBeregning(opprettArenaYtelser(FNR_1, singletonList(
                ArenaVedtakBuilder.create()
                        .dagsats(ZERO)
                        .vedtaksperiode(LocalDate.now(), LocalDate.now().plusDays(30))
                        .build()
        )));
        TrekkResponse response = beregning.apply(request);

        assertThat(response.getTrekkvedtakId(), equalTo(1));
        assertThat(response.getBeslutning(), equalTo(Beslutning.INGEN));
        assertThat(response.getSystem(), equalTo(System.N));
        assertThat(response.getTotalSatsArena(), equalTo(ZERO));
        assertThat(response.getTotalSatsOS(), equalTo(ZERO));
        assertThat(response.getVedtak().size(), equalTo(1));
    }

    @Test
    public void besluttAbetal_TypeProsenttrekk_AktivtIAbetal_YtelseOsLikNull_YtelseAbetalStorreEnnNull() {
        TrekkRequest request = opprettTrekkRequest(Trekkalternativ.LOPP, System.J, ZERO);
        VedtakBeregning beregning = new VedtakBeregning(opprettArenaYtelser(FNR_1, singletonList(
                ArenaVedtakBuilder.create()
                        .dagsats(DAGSATS_1)
                        .vedtaksperiode(NOW, NOW.plusDays(30))
                        .build()
        )));
        TrekkResponse response = beregning.apply(request);

        assertThat(response.getTrekkvedtakId(), equalTo(1));
        assertThat(response.getBeslutning(), equalTo(Beslutning.ABETAL));
        assertThat(response.getSystem(), equalTo(System.J));
        assertThat(response.getTotalSatsArena(), equalTo(DAGSATS_1));
        assertThat(response.getTotalSatsOS(), equalTo(ZERO));
        assertThat(response.getVedtak().size(), equalTo(1));
    }

    @Test
    public void besluttBegge_TypeProsenttrekk_AktivtIAbetal_YtelseOsStorreEnnNull_YtelseAbetalStorreEnnNull() {
        TrekkRequest request = opprettTrekkRequest(Trekkalternativ.LOPP, System.J, DAGSATS_1);
        VedtakBeregning beregning = new VedtakBeregning(opprettArenaYtelser(FNR_1, singletonList(
                ArenaVedtakBuilder.create()
                        .dagsats(DAGSATS_1)
                        .vedtaksperiode(NOW, NOW.plusDays(30))
                        .build()
        )));
        TrekkResponse response = beregning.apply(request);

        assertThat(response.getTrekkvedtakId(), equalTo(1));
        assertThat(response.getBeslutning(), equalTo(Beslutning.BEGGE));
        assertThat(response.getSystem(), equalTo(System.J));
        assertThat(response.getTotalSatsArena(), equalTo(DAGSATS_1));
        assertThat(response.getTotalSatsOS(), equalTo(DAGSATS_1));
        assertThat(response.getVedtak().size(), equalTo(1));
    }

    @Test
    public void besluttIngen_TypeProsenttrekk_AktivtIAbetal_YtelseOsIkkeStorreEnnNull_YtelseAbetalIkkeStorreEnnNull() {
        TrekkRequest request = opprettTrekkRequest(Trekkalternativ.LOPP, System.J, ZERO);
        VedtakBeregning beregning = new VedtakBeregning(opprettArenaYtelser(FNR_1, singletonList(
                ArenaVedtakBuilder.create()
                        .dagsats(ZERO)
                        .vedtaksperiode(NOW, NOW.plusDays(30))
                        .build()
        )));
        TrekkResponse response = beregning.apply(request);

        assertThat(response.getTrekkvedtakId(), equalTo(1));
        assertThat(response.getBeslutning(), equalTo(Beslutning.INGEN));
        assertThat(response.getSystem(), equalTo(System.J));
        assertThat(response.getTotalSatsArena(), equalTo(ZERO));
        assertThat(response.getTotalSatsOS(), equalTo(ZERO));
        assertThat(response.getVedtak().size(), equalTo(1));
    }

    @Test
    public void besluttOs_TypeProsenttrekk_AktivtIAbetal_YtelseOsStorreEnnNull_YtelseAbetalIkkeStorreEnnNull() {
        TrekkRequest request = opprettTrekkRequest(Trekkalternativ.LOPP, System.J, DAGSATS_1);
        VedtakBeregning beregning = new VedtakBeregning(opprettArenaYtelser(FNR_1, singletonList(
                ArenaVedtakBuilder.create()
                        .dagsats(ZERO)
                        .vedtaksperiode(NOW, NOW.plusDays(30))
                        .build()
        )));
        TrekkResponse response = beregning.apply(request);

        assertThat(response.getTrekkvedtakId(), equalTo(1));
        assertThat(response.getBeslutning(), equalTo(Beslutning.OS));
        assertThat(response.getSystem(), equalTo(System.J));
        assertThat(response.getTotalSatsArena(), equalTo(ZERO));
        assertThat(response.getTotalSatsOS(), equalTo(DAGSATS_1));
        assertThat(response.getVedtak().size(), equalTo(1));
    }

    private TrekkRequest opprettTrekkRequest(Trekkalternativ trekkalternativ, System system, BigDecimal totalSatsOS) {
        return TrekkRequestBuilder.create()
                .trekkvedtakId(1)
                .offnr(FNR_1)
                .trekkalt(trekkalternativ)
                .system(system)
                .totalSatsOS(totalSatsOS)
                .build();
    }

    private Map<String, List<ArenaVedtak>> opprettArenaYtelser(String fnr, List<ArenaVedtak> arenaVedtakList) {
        Map<String, List<ArenaVedtak>> ytelsesMap = new HashMap<>();
        ytelsesMap.put(FNR_1, arenaVedtakList);
        return ytelsesMap;
    }

}