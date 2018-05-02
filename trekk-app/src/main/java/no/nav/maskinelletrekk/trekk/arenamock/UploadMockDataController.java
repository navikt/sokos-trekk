package no.nav.maskinelletrekk.trekk.arenamock;

import no.nav.maskinelletrekk.arenamock.v1.ArenaMockData;
import no.nav.maskinelletrekk.arenamock.v1.MockSak;
import no.nav.maskinelletrekk.arenamock.v1.PersonYtelse;
import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.builder.ArenaVedtakBuilder;
import no.nav.maskinelletrekk.trekk.v1.builder.PeriodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class UploadMockDataController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadMockDataController.class);

    private ArenaMockService ytelseVedtakService;

    @Autowired
    public UploadMockDataController(ArenaMockService ytelseVedtakService) {
        Assert.notNull(ytelseVedtakService, "ytelseVedtakService must not be null");
        this.ytelseVedtakService = ytelseVedtakService;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/upload/mock2")
    public ResponseEntity<?> uploadData(@RequestBody String input) {

        LOGGER.info("Mottar mock data: {}", input);
        System.out.println(input);
        ArenaMockData mockData;
        try {
            mockData = JAXB.unmarshal(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), ArenaMockData.class);
        } catch (Exception e) {
            return new ResponseEntity<>("Parsing av XML feilet", HttpStatus.BAD_REQUEST);
        }

        Map<String, List<ArenaVedtak>> listMap = mockData.getPersonYtelseListe().stream()
                .collect(Collectors.toMap(PersonYtelse::getIdent, this::opprettArenaVedtakListe));
        ytelseVedtakService.setMockDataMap(listMap);

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    private List<ArenaVedtak> opprettArenaVedtakListe(PersonYtelse personYtelse) {
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
