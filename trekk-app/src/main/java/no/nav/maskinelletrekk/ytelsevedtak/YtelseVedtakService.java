package no.nav.maskinelletrekk.ytelsevedtak;

import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.TrekkResponse;

import java.util.List;
import java.util.Map;

public interface YtelseVedtakService {

    Map<TrekkRequest, TrekkResponse> hentYtelseskontrakt(List<TrekkRequest> trekkRequestListe);

}
