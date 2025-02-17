package no.nav.sokos.trekk.metrics

import io.micrometer.core.instrument.Timer
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.metrics.core.metrics.Counter

private const val METRICS_NAMESPACE = "trekk"
const val TAG_EXCEPTION_NAME = "exception_name"

object Metrics {
    val prometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    val timer: (metricName: String, className: String, method: String) -> Timer = { metricName, className, method ->
        Timer
            .builder("${METRICS_NAMESPACE}_$metricName")
            .tag("className", className)
            .tag("method", method)
            .description("Timer for database operations")
            .register(prometheusMeterRegistry)
    }

    val soapArenaRequestCounter: Counter =
        Counter
            .builder()
            .name("${METRICS_NAMESPACE}_requests_til_arena_counter")
            .help("Antall SOAP-requests til Arena")
            .withoutExemplars()
            .register(prometheusMeterRegistry.prometheusRegistry)

    val soapArenaResponseCounter: Counter =
        Counter
            .builder()
            .name("${METRICS_NAMESPACE}_response_fra_arena_counter")
            .help("Antall SOAP-responses fra Arena")
            .withoutExemplars()
            .register(prometheusMeterRegistry.prometheusRegistry)

    val soapArenaErrorCounter: Counter =
        Counter
            .builder()
            .name("${METRICS_NAMESPACE}_feilmelding_fra_arena_counterr")
            .help("Antall feilmeldinger returnert fra Arena")
            .labelNames(TAG_EXCEPTION_NAME)
            .withoutExemplars()
            .register(prometheusMeterRegistry.prometheusRegistry)

    val mqTrekkInnMetricCounter: Counter =
        Counter
            .builder()
            .name("${METRICS_NAMESPACE}_trekk_inn_mq_producer")
            .help("Counts the number of trekk sent to OppdragZ through MQ")
            .withoutExemplars()
            .register(prometheusMeterRegistry.prometheusRegistry)

    val mqTrekkInnBoqMetricCounter: Counter =
        Counter
            .builder()
            .name("${METRICS_NAMESPACE}_trekk_inn_boq_mq_producer")
            .help("Counts the number of trekkInnBoq sent to OppdragZ through MQ")
            .withoutExemplars()
            .register(prometheusMeterRegistry.prometheusRegistry)

    val mqBatchReplyMetricCounter: Counter =
        Counter
            .builder()
            .name("${METRICS_NAMESPACE}_mq_batch_reply_producer")
            .help("Counts the number of batch replies sent to OppdragZ through MQ")
            .withoutExemplars()
            .register(prometheusMeterRegistry.prometheusRegistry)

    val mqReplyMetricCounter: Counter =
        Counter
            .builder()
            .name("${METRICS_NAMESPACE}_mq_reply_producer")
            .help("Counts the number of replies sent to OppdragZ through MQ")
            .withoutExemplars()
            .register(prometheusMeterRegistry.prometheusRegistry)
}
