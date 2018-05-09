package no.nav.maskinelletrekk.trekk.arenamock;

import no.nav.maskinelletrekk.arenamock.v1.ArenaMockData;
import no.nav.maskinelletrekk.arenamock.v1.PersonYtelse;
import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/arenamock")
public class ArenaMockController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArenaMockController.class);

    private ArenaMockService ytelseVedtakService;

    private ArenaMockDataMapper arenaMockDataMapper;

    @Autowired
    public ArenaMockController(ArenaMockService ytelseVedtakService,
                               ArenaMockDataMapper arenaMockDataMapper) {
        Assert.notNull(ytelseVedtakService, "ytelseVedtakService must not be null");
        Assert.notNull(arenaMockDataMapper, "arenaMockDataMapper must not be null");
        this.ytelseVedtakService = ytelseVedtakService;
        this.arenaMockDataMapper = arenaMockDataMapper;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/data")
    public String visData(Model model) {
        model.addAttribute("testdata", ytelseVedtakService.getMockDataMap());
        model.addAttribute("xml", ytelseVedtakService.getMockDataXml());
        return "data";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/data")
    public String visData(@RequestParam(name = "input") String input, RedirectAttributes redirectAttributes) {
        LOGGER.info("Mottar mock data: {}", input);
        try {
            lagreData(input);
        } catch (FeilMedTestdataException e) {
            LOGGER.error("Feil ved parsing", e);
            redirectAttributes.addFlashAttribute("feilmelding", "Feil ved parsing av mock-data");
        }
        return "redirect:data";
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    public ResponseEntity<String> postMockData(@RequestBody String input) {
        LOGGER.info("Mottar mock data: {}", input);
        try {
            lagreData(input);
        } catch (FeilMedTestdataException e) {
            LOGGER.error("Feil ved parsing", e);
            return new ResponseEntity<>(e.getCause().getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    private void lagreData(String input) throws FeilMedTestdataException {
        ArenaMockData mockData;
        try {
            mockData = JAXB.unmarshal(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), ArenaMockData.class);
        } catch (Exception e) {
            LOGGER.error("Feil ved parsing: {}", e);
            throw new FeilMedTestdataException("Feil med testdata", e);
        }
        ytelseVedtakService.setKjoreDato(getKjoredato(mockData));
        ytelseVedtakService.setMockDataXml(input);
        Map<String, List<ArenaVedtak>> listMap = mockData.getPersonYtelse().stream()
                .collect(Collectors.toMap(PersonYtelse::getIdent, arenaMockDataMapper));
        ytelseVedtakService.setMockDataMap(listMap);
    }

    private LocalDate getKjoredato(ArenaMockData mockData) {
        return mockData.getKjoreDato() == null ? LocalDate.now() : mockData.getKjoreDato();
    }

}
