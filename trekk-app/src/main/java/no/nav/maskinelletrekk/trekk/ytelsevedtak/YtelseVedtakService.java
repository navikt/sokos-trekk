package no.nav.maskinelletrekk.trekk.ytelsevedtak;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;

import java.util.List;
import java.util.Map;

public interface YtelseVedtakService {

    Map<String, List<ArenaVedtak>> hentYtelseskontrakt(List<TrekkRequest> trekkRequestListe);

}
