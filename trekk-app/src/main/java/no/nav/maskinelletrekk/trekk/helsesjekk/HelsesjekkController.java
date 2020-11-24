package no.nav.maskinelletrekk.trekk.helsesjekk;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static no.nav.maskinelletrekk.trekk.config.Metrics.isReady;

@Controller
public class HelsesjekkController {

    @ResponseBody
    @RequestMapping(value = "isAlive", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> isAlive() {
        return new ResponseEntity<>("Alive", HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = "isReady", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> isReady() {
        isReady.set(1);
        return new ResponseEntity<>("Ready", HttpStatus.OK);
    }
}