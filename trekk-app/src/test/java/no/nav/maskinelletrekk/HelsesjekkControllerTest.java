package no.nav.maskinelletrekk;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HelsesjekkControllerTest {

    private HelsesjekkController controller = new HelsesjekkController();

    @Test
    public void isAlive() {
        ResponseEntity alive = controller.isAlive();
        Assert.assertThat(alive.getStatusCode(), CoreMatchers.equalTo(HttpStatus.OK));
    }

    @Test
    public void isReady() {
        ResponseEntity ready = controller.isReady();
        Assert.assertThat(ready.getStatusCode(), CoreMatchers.equalTo(HttpStatus.OK));
    }
}