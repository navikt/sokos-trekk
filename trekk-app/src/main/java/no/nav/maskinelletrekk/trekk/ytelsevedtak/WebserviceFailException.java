package no.nav.maskinelletrekk.trekk.ytelsevedtak;

class WebserviceFailException extends RuntimeException {

    private static final long serialVersionUID = 5602620786014811068L;

    WebserviceFailException(String message, Throwable cause) {
        super(message, cause);
    }
}
