package no.nav.maskinelletrekk.trekk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Bootstrap {

    private Bootstrap() {
    }

    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);
    }

}
