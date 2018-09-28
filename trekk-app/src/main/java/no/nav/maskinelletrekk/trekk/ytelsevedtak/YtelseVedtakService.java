package no.nav.maskinelletrekk.trekk.ytelsevedtak;

import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface YtelseVedtakService {

    Map<String, List<ArenaVedtak>> hentYtelseskontrakt(Set<String> brukerList, LocalDate fom, LocalDate tom);

}
