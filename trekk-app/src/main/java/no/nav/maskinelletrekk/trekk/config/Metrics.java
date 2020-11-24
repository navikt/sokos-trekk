package no.nav.maskinelletrekk.trekk.config;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

public final class Metrics {

    private static final String NAMESPACE = "trekk";

    private static final String LABEL_QUEUE = "queue";
    private static final String LABEL_EXCEPTION_NAME = "exception_name";

    public static final Gauge isReady = Gauge.build()
            .namespace(NAMESPACE)
            .name("app_is_ready")
            .help("App is ready to recieve traffic")
            .register();

    public static final Counter meldingerFraOSCounter = Counter.build()
            .namespace(NAMESPACE)
            .name("meldinger_fra_os")
            .help("Antall meldinger mottatt fra OS")
            .register();

    public static final Counter aggregerteMeldingerFraOSCounter = Counter.build()
            .namespace(NAMESPACE)
            .name("aggregerte_meldinger_fra_os")
            .help("Antall aggregerte meldinger mottatt fra OS")
            .register();

    public static final Counter meldingerTilOSCounter = Counter.build()
            .namespace(NAMESPACE)
            .name("meldinger_til_os")
            .help("Antall meldinger lagt på reply-kø til OS")
            .labelNames(LABEL_QUEUE)
            .register();

    public static final Counter meldingerTilArenaCounter = Counter.build()
            .namespace(NAMESPACE)
            .name("requests_til_arena")
            .help("Antall SOAP-requests til Arena")
            .register();

    public static final Counter meldingerFraArenaCounter = Counter.build()
            .namespace(NAMESPACE)
            .name("response_fra_arena")
            .help("Antall SOAP-responses fra Arena")
            .register();

    public static final Counter feilmeldingerArenaCounter = Counter.build()
            .namespace(NAMESPACE)
            .name("feilmeldinger_fra_arena")
            .help("Antall feilmeldinger returnert fra Arena")
            .labelNames("exception")
            .register();


    public static final Counter boqCounter = Counter.build()
            .namespace(NAMESPACE)
            .name("messages_on_boq")
            .help("Counts messages put on backout queue")
            .labelNames(LABEL_EXCEPTION_NAME)
            .register();

    public static final Counter beslutningerCounter = Counter.build()
            .namespace(NAMESPACE)
            .name("beslutning")
            .help("Trekk beslutning basert på trekkalternativ og system")
            .labelNames("trekkalternativ", "system", "beslutning")
            .register();

    private Metrics() {
    }
}
