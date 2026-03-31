package no.nav.sokos.trekk.exception

class TrekkException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
