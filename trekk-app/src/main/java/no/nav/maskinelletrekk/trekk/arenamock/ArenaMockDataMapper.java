package no.nav.maskinelletrekk.trekk.arenamock;

import no.nav.maskinelletrekk.arenamock.v1.MockSak;
import no.nav.maskinelletrekk.arenamock.v1.PersonYtelse;
import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.builder.ArenaVedtakBuilder;
import no.nav.maskinelletrekk.trekk.v1.builder.PeriodeBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArenaMockDataMapper implements Function<PersonYtelse, List<ArenaVedtak>> {

    @Override
    public List<ArenaVedtak> apply(PersonYtelse personYtelse) {
        return personYtelse.getSak().stream().flatMap(this::mapSak).collect(Collectors.toList());
    }

    private Stream<? extends ArenaVedtak> mapSak(MockSak sak) {
        String tema = sak.getTema().value();
        return sak.getVedtak().stream()
                .map(vedtak ->
                        ArenaVedtakBuilder.create()
                                .dagsats(BigDecimal.valueOf(vedtak.getDagsats()))
                                .rettighetType(vedtak.getRettighetstype().value())
                                .tema(tema)
                                .vedtaksperiode(PeriodeBuilder.create()
                                        .fom(vedtak.getVedtaksperiode().getFom())
                                        .tom(vedtak.getVedtaksperiode().getTom())
                                        .build())
                                .build()
                );
    }
}
