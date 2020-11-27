package no.nav.maskinelletrekk.trekk.ytelsevedtak;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.FinnYtelseVedtakListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.FinnYtelseVedtakListeUgyldigInput;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.YtelseVedtakV1;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Periode;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.PersonYtelse;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.informasjon.Tema;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeRequest;
import no.nav.tjeneste.virksomhet.ytelsevedtak.v1.meldinger.FinnYtelseVedtakListeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeConfigurationException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static no.nav.maskinelletrekk.trekk.config.Metrikker.FEILMELDING_FRA_ARENA_COUNTER;
import static no.nav.maskinelletrekk.trekk.config.Metrikker.KALL_TIL_ARENA_TIMER;
import static no.nav.maskinelletrekk.trekk.config.Metrikker.REQUESTS_TIL_ARENA_COUNTER;
import static no.nav.maskinelletrekk.trekk.config.Metrikker.RESPONSE_FRA_ARENA_COUNTER;
import static no.nav.maskinelletrekk.trekk.config.Metrikker.TAG_EXCEPTION_NAME;

@Service
@Profile({"prod"})
public class ArenaYtelseVedtakService implements YtelseVedtakService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArenaYtelseVedtakService.class);

    private final YtelseVedtakV1 ytelseVedtakService;
    private final SakTilVedtakMapper sakTilVedtakMapper;

    @Autowired
    public ArenaYtelseVedtakService(YtelseVedtakV1 ytelseVedtakService, SakTilVedtakMapper sakTilVedtakMapper) {
        this.ytelseVedtakService = requireNonNull(ytelseVedtakService, "ytelseVedtakService must not be null");
        this.sakTilVedtakMapper = requireNonNull(sakTilVedtakMapper, "sakTilVedtakMapper must not be null");
    }

    @Override
    public Map<String, List<ArenaVedtak>> hentYtelseskontrakt(Set<String> brukerSet, LocalDate fom, LocalDate tom) {
        FinnYtelseVedtakListeRequest request = opprettFinnYtelseVedtakListeRequest(brukerSet, fom, tom);
        FinnYtelseVedtakListeResponse response = kallArenaYtelseVedtakService(request);

        return response.getPersonYtelseListe().stream()
                .collect(toMap(PersonYtelse::getIdent, this::opprettArenaVedtakListe));
    }

    private List<ArenaVedtak> opprettArenaVedtakListe(PersonYtelse personYtelse) {
        return personYtelse.getSakListe().stream()
                .flatMap(sakTilVedtakMapper)
                .collect(Collectors.toList());
    }

    @Timed(KALL_TIL_ARENA_TIMER)
    private FinnYtelseVedtakListeResponse kallArenaYtelseVedtakService(FinnYtelseVedtakListeRequest request) {
        FinnYtelseVedtakListeResponse response;
        try {
            Metrics.counter(REQUESTS_TIL_ARENA_COUNTER).increment();
            response = ytelseVedtakService.finnYtelseVedtakListe(request);
            Metrics.counter(RESPONSE_FRA_ARENA_COUNTER).increment();
        } catch (FinnYtelseVedtakListeUgyldigInput | FinnYtelseVedtakListeSikkerhetsbegrensning e) {
            Metrics.counter(FEILMELDING_FRA_ARENA_COUNTER, TAG_EXCEPTION_NAME, e.getClass().getSimpleName()).increment();
            throw new WebserviceFailException("Kall mot Arena feilet", e);
        }
        return response;
    }

    private FinnYtelseVedtakListeRequest opprettFinnYtelseVedtakListeRequest(Set<String> brukerSet,
                                                                             LocalDate fom,
                                                                             LocalDate tom) {
        FinnYtelseVedtakListeRequest request = new FinnYtelseVedtakListeRequest();
        request.getPersonListe().addAll(opprettPersonListe(brukerSet, opprettPeriode(fom, tom)));
        request.getTemaListe().addAll(opprettTema("AAP", "DAG", "IND"));
        return request;
    }

    private Set<Person> opprettPersonListe(Set<String> brukerSet, Periode periode) {
        return brukerSet.stream()
                .map(fnr -> opprettPerson(fnr, periode))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Person opprettPerson(String fnr, Periode periode) {
        Person person = new Person();
        person.setIdent(fnr);
        person.setPeriode(periode);
        return person;
    }

    private Periode opprettPeriode(LocalDate fom, LocalDate tom) {
        try {
            Periode periode = new Periode();
            periode.setFom(DateMapper.toXmlGregorianCalendar(fom));
            periode.setTom(DateMapper.toXmlGregorianCalendar(tom));
            return periode;
        } catch (DatatypeConfigurationException e) {
            throw new FeilVedOpprettelseAvRequestException("Feil ved parsing av dato", e);
        }
    }

    private Set<Tema> opprettTema(String... temanavn) {
        Set<Tema> temaSet = new HashSet<>();
        for (String s : temanavn) {
            Tema tema = new Tema();
            tema.setValue(s);
            temaSet.add(tema);
        }
        return temaSet;
    }

}
