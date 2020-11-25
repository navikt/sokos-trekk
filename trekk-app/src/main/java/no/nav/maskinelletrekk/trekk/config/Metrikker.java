package no.nav.maskinelletrekk.trekk.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public final class Metrikker {

    public static final String PREFIX = "trekk_";

    public static final String BESLUTNING_COUNTER = PREFIX + "beslutning_counter";
    public static final String MELDING_TIL_BOQ_COUNTER = PREFIX + "melding_til_boq_counter";
    public static final String FEILMELDING_FRA_ARENA_COUNTER = PREFIX + "feilmelding_fra_arena_counter";
    public static final String RESPONSE_FRA_ARENA_COUNTER = PREFIX + "response_fra_arena_counter";
    public static final String REQUESTS_TIL_ARENA_COUNTER = PREFIX + "requests_til_arena_counter";
    public static final String MELDING_TIL_OS_COUNTER = PREFIX + "melding_til_os_counter";
    public static final String AGGREGERT_MELDING_FRA_OS_COUNTER = PREFIX + "aggregert_melding_fra_os_counter";
    public static final String MELDING_FRA_OS_COUNTER = PREFIX + "antall_melding_fra_os_counter";
    public static final String GYLDIG_MELDING_FRA_OS_COUNTER = PREFIX + "gyldig_melding_fra_os_counter";

    public static final String TAG_LABEL_QUEUE = "queue";
    public static final String TAG_EXCEPTION_NAME = "exception_name";

    @Autowired
    public Metrikker(MeterRegistry registry) {
        Objects.requireNonNull(registry);
        Counter.builder(MELDING_FRA_OS_COUNTER)
                .description("Totalt antall meldinger mottatt fra OS")
                .register(registry);
        Counter.builder(GYLDIG_MELDING_FRA_OS_COUNTER)
                .description("Antall meldinger mottatt fra OS som validerer")
                .register(registry);
        Counter.builder(AGGREGERT_MELDING_FRA_OS_COUNTER)
                .description("Antall aggregerte meldinger mottatt fra OS")
                .register(registry);
        Counter.builder(MELDING_TIL_OS_COUNTER)
                .description("Antall meldinger lagt på reply-kø til OS")
                .tags(TAG_LABEL_QUEUE, "")
                .register(registry);
        Counter.builder(REQUESTS_TIL_ARENA_COUNTER)
                .description("Antall SOAP-requests til Arena")
                .register(registry);
        Counter.builder(RESPONSE_FRA_ARENA_COUNTER)
                .description("Antall SOAP-responses fra Arena")
                .register(registry);
        Counter.builder(FEILMELDING_FRA_ARENA_COUNTER)
                .description("Antall feilmeldinger returnert fra Arena")
                .tags(TAG_EXCEPTION_NAME, "")
                .register(registry);
        Counter.builder(MELDING_TIL_BOQ_COUNTER)
                .description("Counts messages put on backout queue")
                .tags(TAG_EXCEPTION_NAME, "")
                .register(registry);
        Counter.builder(BESLUTNING_COUNTER)
                .description("Trekk beslutning basert på trekkalternativ og system")
                .tags("trekkalternativ", "", "system", "", "beslutning", "")
                .register(registry);
    }
}
