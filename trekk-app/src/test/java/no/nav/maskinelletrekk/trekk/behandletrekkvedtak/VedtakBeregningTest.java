package no.nav.maskinelletrekk.trekk.behandletrekkvedtak;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.TrekkResponse;
import no.nav.maskinelletrekk.trekk.v1.builder.ArenaVedtakBuilder;
import no.nav.maskinelletrekk.trekk.v1.builder.TrekkRequestBuilder;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class VedtakBeregningTest {

    private static final String FNR_1 = "12312312312";
    private static final BigDecimal DAGSATS = BigDecimal.valueOf(123);

    @Test
    public void skalInkluderePeriodeMedTomNullRequestPeriode() {
        Map<String, List<ArenaVedtak>> ytelsesMap = new HashMap<>();
        ytelsesMap.put(FNR_1, Arrays.asList(
                ArenaVedtakBuilder.create()
                        .dagsats(DAGSATS)
                        .vedtaksperiode(LocalDate.now().minusDays(10), null)
                        .build(),
                ArenaVedtakBuilder.create()
                        .dagsats(DAGSATS)
                        .vedtaksperiode(LocalDate.now(), null)
                        .build(),
                ArenaVedtakBuilder.create()
                        .dagsats(DAGSATS)
                        .vedtaksperiode(LocalDate.now().plusDays(10), null)
                        .build()
                )
        );

        VedtakBeregning beregning = new VedtakBeregning(ytelsesMap);

        TrekkResponse response = beregning.apply(getTrekkRequestOgPeriode());

        assertThat(response.getTrekkvedtakId(), equalTo(1));
        assertThat(response.getVedtak().size(), equalTo(3));
    }

    @Test
    public void skalInkluderePeriodeLengerEnnRequestPeriode() {
        Map<String, List<ArenaVedtak>> ytelsesMap = new HashMap<>();
        ytelsesMap.put(FNR_1, Collections.singletonList(
                ArenaVedtakBuilder.create()
                        .dagsats(DAGSATS)
                        .vedtaksperiode(LocalDate.now().minusDays(100), LocalDate.now().plusDays(100))
                        .build())
        );

        VedtakBeregning beregning = new VedtakBeregning(ytelsesMap);

        TrekkResponse response = beregning.apply(getTrekkRequestOgPeriode());

        assertThat(response.getTrekkvedtakId(), equalTo(1));
        assertThat(response.getVedtak().size(), equalTo(1));
    }

    @Test
    public void skalInkluderePeriodeInnenForRequestPeriode() {
        Map<String, List<ArenaVedtak>> ytelsesMap = new HashMap<>();
        ytelsesMap.put(FNR_1, Collections.singletonList(
                ArenaVedtakBuilder.create()
                        .dagsats(DAGSATS)
                        .vedtaksperiode(LocalDate.now().plusDays(1), LocalDate.now().plusDays(9))
                        .build())
        );

        VedtakBeregning beregning = new VedtakBeregning(ytelsesMap);

        TrekkResponse response = beregning.apply(getTrekkRequestOgPeriode());

        assertThat(response.getTrekkvedtakId(), equalTo(1));
        assertThat(response.getVedtak().size(), equalTo(1));
    }

    @Test
    public void skalInkluderePeriodeSomToucherFoerOgEtterRequestPeriode() {
        Map<String, List<ArenaVedtak>> ytelsesMap = new HashMap<>();
        ytelsesMap.put(FNR_1, Arrays.asList(
                ArenaVedtakBuilder.create()
                        .dagsats(DAGSATS)
                        .vedtaksperiode(LocalDate.now().minusDays(10), LocalDate.now())
                        .build(),
                ArenaVedtakBuilder.create()
                        .dagsats(DAGSATS)
                        .vedtaksperiode(LocalDate.now(), LocalDate.now().plusDays(10))
                        .build()
                )
        );

        VedtakBeregning beregning = new VedtakBeregning(ytelsesMap);

        TrekkResponse response = beregning.apply(getTrekkRequestOgPeriode());

        assertThat(response.getTrekkvedtakId(), equalTo(1));
        assertThat(response.getVedtak().size(), equalTo(2));
    }

    @Test
    public void skalIkkeInkluderePeriodeFoerEllerEtterRequestPeriode() {
        Map<String, List<ArenaVedtak>> ytelsesMap = new HashMap<>();
        ytelsesMap.put(FNR_1, Arrays.asList(
                ArenaVedtakBuilder.create()
                        .dagsats(DAGSATS)
                        .vedtaksperiode(LocalDate.now().minusDays(10), LocalDate.now().minusDays(1))
                        .build(),
                ArenaVedtakBuilder.create()
                        .dagsats(DAGSATS)
                        .vedtaksperiode(LocalDate.now().plusDays(11), LocalDate.now().plusDays(12))
                        .build())
        );

        VedtakBeregning beregning = new VedtakBeregning(ytelsesMap);

        TrekkResponse response = beregning.apply(getTrekkRequestOgPeriode());

        assertThat(response.getTrekkvedtakId(), equalTo(1));
        assertThat(response.getVedtak().size(), equalTo(0));
    }

    private TrekkRequestOgPeriode getTrekkRequestOgPeriode() {
        return new TrekkRequestOgPeriode(
                TrekkRequestBuilder.create()
                        .bruker(FNR_1)
                        .trekkvedtakId(1)
                        .antallDager(10)
                        .oppdragsvedtak()
                        .build());
    }

}