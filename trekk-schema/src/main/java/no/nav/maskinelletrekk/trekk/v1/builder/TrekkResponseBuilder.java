package no.nav.maskinelletrekk.trekk.v1.builder;

import no.nav.maskinelletrekk.trekk.v1.Abetal;
import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.Beslutning;
import no.nav.maskinelletrekk.trekk.v1.TrekkResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TrekkResponseBuilder {

    private int trekkvedtakId;
    private Beslutning beslutning;
    private Abetal abetal;
    private BigDecimal totalSatsOS;
    private BigDecimal totalSatsArena;
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

    public TrekkResponseBuilder abetal(Abetal abetal) {
        this.abetal = abetal;
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
        trekkResponse.setAbetal(abetal);
        trekkResponse.setTotalSatsOS(totalSatsOS);
        trekkResponse.setTotalSatsArena(totalSatsArena);
        trekkResponse.getVedtak().addAll(vedtak);
        return trekkResponse;
    }
}
