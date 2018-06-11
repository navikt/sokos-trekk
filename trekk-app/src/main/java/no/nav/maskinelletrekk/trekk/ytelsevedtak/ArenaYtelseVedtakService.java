package no.nav.maskinelletrekk.trekk.ytelsevedtak;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.xml.bind.JAXB;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.StringWriter;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Service
public class ArenaYtelseVedtakService implements YtelseVedtakService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArenaYtelseVedtakService.class);

    private YtelseVedtakV1 ytelseVedtakService;
    private SakTilVedtakMapper sakTilVedtakMapper;
    private Clock clock;

    @Autowired
    public ArenaYtelseVedtakService(YtelseVedtakV1 ytelseVedtakService,
                                    SakTilVedtakMapper sakTilVedtakMapper,
                                    Clock clock) {
        Assert.notNull(ytelseVedtakService, "ytelseVedtakService must not be null");
        Assert.notNull(sakTilVedtakMapper, "sakTilVedtakMapper must not be null");
        Assert.notNull(clock, "clock must not be null");
        this.ytelseVedtakService = ytelseVedtakService;
        this.sakTilVedtakMapper = sakTilVedtakMapper;
        this.clock = clock;
    }

    @Override
    public Map<String, List<ArenaVedtak>> hentYtelseskontrakt(List<TrekkRequest> trekkRequestListe) {
        FinnYtelseVedtakListeRequest request = opprettFinnYtelseVedtakListeRequest(trekkRequestListe);
        loggSoapRequest(request);
        FinnYtelseVedtakListeResponse response = kallArenaYtelseVedtakService(request);
        loggSoapResponse(response);

        return response.getPersonYtelseListe().stream()
                .collect(toMap(PersonYtelse::getIdent, this::opprettArenaVedtakListe));
    }

    @SuppressWarnings("squid:UnusedPrivateMethod") // Brukes i metoden over.
    private List<ArenaVedtak> opprettArenaVedtakListe(PersonYtelse personYtelse) {
        return personYtelse.getSakListe().stream()
                .flatMap(sakTilVedtakMapper)
                .collect(Collectors.toList());
    }

    private FinnYtelseVedtakListeResponse kallArenaYtelseVedtakService(FinnYtelseVedtakListeRequest request) {
        FinnYtelseVedtakListeResponse response;
        try {
            response = ytelseVedtakService.finnYtelseVedtakListe(request);
        } catch (FinnYtelseVedtakListeUgyldigInput | FinnYtelseVedtakListeSikkerhetsbegrensning e) {
            throw new WebserviceFailException("Kall mot Arena feilet", e);
        }
        return response;
    }

    private FinnYtelseVedtakListeRequest opprettFinnYtelseVedtakListeRequest(List<TrekkRequest> trekkRequestListe) {
        FinnYtelseVedtakListeRequest request = new FinnYtelseVedtakListeRequest();
        request.getPersonListe().addAll(opprettPersonListe(trekkRequestListe));
        request.getTemaListe().addAll(getTema("AAP", "DAG", "IND"));
        return request;
    }

    private Set<Person> opprettPersonListe(List<TrekkRequest> trekkRequestListe) {
        return trekkRequestListe.stream()
                .peek(request -> LOGGER.info("Legger til TrekkRequest " +
                                "[trekkVedtakId: {}, bruker: {}] i request til Arena",
                        request.getTrekkvedtakId(), request.getBruker()))
                .map(this::opprettPerson)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Person opprettPerson(TrekkRequest trekkRequest) {
        try {
            int antallDager = trekkRequest.getAntallDager();
            LocalDate now = LocalDate.now(clock);

            Periode periode = new Periode();
            periode.setFom(DateUtil.toXmlGregorianCalendar(now));
            periode.setTom(DateUtil.toXmlGregorianCalendar(now.plusDays(antallDager)));

            Person person = new Person();
            person.setIdent(trekkRequest.getBruker());
            person.setPeriode(periode);

            return person;
        } catch (DatatypeConfigurationException e) {
            throw new FeilVedOpprettelseAvRequestException("Feil ved parsing av dato", e);
        }
    }

    private Set<Tema> getTema(String... temanavn) {
        Set<Tema> temaList = new HashSet<>();
        for (String s : temanavn) {
            Tema tema = new Tema();
            tema.setValue(s);
            temaList.add(tema);
        }
        return temaList;
    }

    private void loggSoapResponse(Object request) {
        StringWriter ws = new StringWriter();
        JAXB.marshal(request, ws);
        LOGGER.info("Mottatt melding fra Arena: {}", ws.toString());
    }

    private void loggSoapRequest(Object request) {
        StringWriter ws = new StringWriter();
        JAXB.marshal(request, ws);
        LOGGER.info("Sender melding til Arena: {}", ws.toString());
    }
}
