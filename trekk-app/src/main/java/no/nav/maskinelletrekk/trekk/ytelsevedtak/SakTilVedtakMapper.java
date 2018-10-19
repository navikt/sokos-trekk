package no.nav.maskinelletrekk.trekk.ytelsevedtak;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.builder.ArenaVedtakBuilder;
import no.nav.maskinelletrekk.trekk.v1.builder.PeriodeBuilder;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Sak;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Vedtak;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static no.nav.maskinelletrekk.trekk.behandletrekkvedtak.VedtakBeregning.SUM_SCALE;
import static no.nav.maskinelletrekk.trekk.ytelsevedtak.DateMapper.toLocalDate;

@Component
@Profile({"prod"})
public class SakTilVedtakMapper implements Function<Sak, Stream<? extends ArenaVedtak>> {

    @Override
    public Stream<? extends ArenaVedtak> apply(Sak sak) {
        return sak.getVedtakListe().stream()
                .map(mapVedtak(sak.getTema().getKodeverksRef()));
    }

    private Function<Vedtak, ArenaVedtak> mapVedtak(String tema) {
        return vedtak -> ArenaVedtakBuilder.create()
                .dagsats(BigDecimal.valueOf(vedtak.getDagsats()).setScale(SUM_SCALE, ROUND_HALF_UP))
                .rettighetType(vedtak.getRettighetstype().getKodeverksRef())
                .tema(tema)
                .vedtaksperiode(PeriodeBuilder.create()
                        .fom(toLocalDate(vedtak.getVedtaksperiode().getFom()))
                        .tom(toLocalDate(vedtak.getVedtaksperiode().getTom()))
                        .build())
                .build();
    }
}
