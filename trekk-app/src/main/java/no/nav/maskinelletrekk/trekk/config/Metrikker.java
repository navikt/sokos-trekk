package no.nav.maskinelletrekk.trekk.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public final class Metrikker {

    public static final String TAG_LABEL_QUEUE = "queue";
    public static final String TAG_EXCEPTION_NAME = "exception_name";
    public static final String BESLUTNING_COUNTER = "beslutning";
    public static final String MESSAGES_ON_BOQ = "messages_on_boq";
    public static final String FEILMELDINGER_FRA_ARENA = "feilmeldinger_fra_arena";
    public static final String RESPONSE_FRA_ARENA = "response_fra_arena";
    public static final String REQUESTS_TIL_ARENA = "requests_til_arena";
    public static final String MELDINGER_TIL_OS = "meldinger_til_os";
    public static final String AGGREGERTE_MELDINGER_FRA_OS = "aggregerte_meldinger_fra_os";
    public static final String MELDINGER_FRA_OS = "meldinger_fra_os";

    @Autowired
    public Metrikker(MeterRegistry registry) {
        Objects.requireNonNull(registry);
        Counter.builder(MELDINGER_FRA_OS)
                .description("Antall meldinger mottatt fra OS")
                .register(registry);
        Counter.builder(AGGREGERTE_MELDINGER_FRA_OS)
                .description("Antall aggregerte meldinger mottatt fra OS")
                .register(registry);
        Counter.builder(MELDINGER_TIL_OS)
                .description("Antall meldinger lagt på reply-kø til OS")
                .tags(TAG_LABEL_QUEUE, "")
                .register(registry);
        Counter.builder(REQUESTS_TIL_ARENA)
                .description("Antall SOAP-requests til Arena")
                .register(registry);
        Counter.builder(RESPONSE_FRA_ARENA)
                .description("Antall SOAP-responses fra Arena")
                .register(registry);
        Counter.builder(FEILMELDINGER_FRA_ARENA)
                .description("Antall feilmeldinger returnert fra Arena")
                .tags(TAG_EXCEPTION_NAME, "")
                .register(registry);
        Counter.builder(MESSAGES_ON_BOQ)
                .description("Counts messages put on backout queue")
                .tags(TAG_EXCEPTION_NAME, "")
                .register(registry);
        Counter.builder(BESLUTNING_COUNTER)
                .description("Trekk beslutning basert på trekkalternativ og system")
                .tags("trekkalternativ", "", "system", "", "beslutning", "")
                .register(registry);
    }
}
