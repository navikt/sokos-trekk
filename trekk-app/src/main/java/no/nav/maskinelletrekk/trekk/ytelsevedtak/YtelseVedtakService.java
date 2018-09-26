package no.nav.maskinelletrekk.trekk.ytelsevedtak;

import no.nav.maskinelletrekk.trekk.behandletrekkvedtak.TrekkOgPeriode;
import no.nav.maskinelletrekk.trekk.v1.ArenaVedtak;

import java.util.List;
import java.util.Map;

public interface YtelseVedtakService {

    Map<String, List<ArenaVedtak>> hentYtelseskontrakt(TrekkOgPeriode trekkOgPeriode);

}
