package no.nav.maskinelletrekk.trekk.helsesjekk;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HelsesjekkControllerTest {

    private HelsesjekkController controller = new HelsesjekkController();

    @Test
    public void erAlive() {
        ResponseEntity alive = controller.isAlive();
        Assert.assertThat(alive.getStatusCode(), CoreMatchers.equalTo(HttpStatus.OK));
    }

    @Test
    public void erReady() {
        ResponseEntity ready = controller.isReady();
        Assert.assertThat(ready.getStatusCode(), CoreMatchers.equalTo(HttpStatus.OK));
    }

}