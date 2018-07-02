package no.nav.maskinelletrekk.trekk.config;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

import static no.nav.maskinelletrekk.trekk.config.PrometheusLabels.LABEL_ERROR_TYPE;
import static no.nav.maskinelletrekk.trekk.config.PrometheusLabels.LABEL_EVENT;
import static no.nav.maskinelletrekk.trekk.config.PrometheusLabels.LABEL_EXCEPTION_NAME;
import static no.nav.maskinelletrekk.trekk.config.PrometheusLabels.LABEL_PROCESS;

public final class PrometheusMetrics {

    private static final String NAMESPACE = "trekk";

    public static final Gauge isReady = Gauge.build()
            .namespace(NAMESPACE)
            .name("app_is_ready")
            .help("App is ready to recieve traffic")
            .register();

    public static final Counter meldingerFraOSCounter = Counter.build()
            .namespace(NAMESPACE)
            .name("trekk_meldinger_fra_os")
            .help("Antall meldinger mottatt fra OS")
            .labelNames(LABEL_PROCESS, LABEL_EVENT)
            .register();

    public static final Counter aggregerteMeldingerFraOSCounter = Counter.build()
            .namespace(NAMESPACE)
            .name("trekk_aggregerte_meldinger_fra_os")
            .help("Antall aggregerte meldinger mottatt fra OS")
            .labelNames(LABEL_PROCESS, LABEL_EVENT)
            .register();

    public static final Counter meldingerTilOSCounter = Counter.build()
            .namespace(NAMESPACE)
            .name("trekk_meldinger_til_os")
            .help("Antall meldinger lagt på reply-kø til OS")
            .labelNames(LABEL_PROCESS, LABEL_EVENT)
            .register();

    public static final Counter meldingerTilArenaCounter = Counter.build()
            .namespace(NAMESPACE)
            .name("trekk_requests_til_arena")
            .help("Antall SOAP-requests til Arena")
            .labelNames(LABEL_PROCESS, LABEL_EVENT)
            .register();

    public static final Counter meldingerFraArenaCounter = Counter.build()
            .namespace(NAMESPACE)
            .name("trekk_response_fra_arena")
            .help("Antall SOAP-responses fra Arena")
            .labelNames(LABEL_PROCESS, LABEL_EVENT)
            .register();

    public static final Counter exceptionCounter = Counter.build()
            .namespace(NAMESPACE)
            .name("trekk_exception_total_counter")
            .help("Counts total number of exceptions")
            .labelNames(LABEL_PROCESS, LABEL_ERROR_TYPE, LABEL_EXCEPTION_NAME)
            .register();

    private PrometheusMetrics() {
    }
}
