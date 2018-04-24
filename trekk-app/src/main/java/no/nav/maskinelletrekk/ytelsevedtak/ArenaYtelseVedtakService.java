package no.nav.maskinelletrekk.ytelsevedtak;

import no.nav.maskinelletrekk.trekk.v1.Beslutning;
import no.nav.maskinelletrekk.trekk.v1.ObjectFactory;
import no.nav.maskinelletrekk.trekk.v1.Oppdragsvedtak;
import no.nav.maskinelletrekk.trekk.v1.TrekkRequest;
import no.nav.maskinelletrekk.trekk.v1.TrekkResponse;
import no.nav.maskinelletrekk.trekk.v1.Vedtak;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ArenaYtelseVedtakService implements YtelseVedtakService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArenaYtelseVedtakService.class);

    @Override
    public Map<TrekkRequest, TrekkResponse> hentYtelseskontrakt(List<TrekkRequest> trekkRequestListe) {
        Map<TrekkRequest, TrekkResponse> resultMap = new HashMap<>();
        for (TrekkRequest trekkRequest : trekkRequestListe) {
            LOGGER.info("Behandler trekkvedtak med ID {} ",
                    trekkRequest.getTrekkvedtakId());
            resultMap.put(trekkRequest, mockResponse(trekkRequest));
        }
        return resultMap;
    }

    private TrekkResponse mockResponse(TrekkRequest trekkRequest) {

        TrekkResponse trekkResponse = new ObjectFactory().createTrekkResponse();

        BigDecimal sumOs = trekkRequest.getOppdragsvedtak().stream().map(Oppdragsvedtak::getSats).reduce(BigDecimal.ZERO, BigDecimal::add);

        trekkResponse.setTrekkvedtakId(trekkRequest.getTrekkvedtakId());
        trekkResponse.setTotalSatsOS(sumOs);
        trekkResponse.setTotalSatsArena(sumOs);
        trekkResponse.setBeslutning(Beslutning.ABETAL);
        trekkResponse.getVedtak().addAll(trekkRequest.getOppdragsvedtak().stream().map(ArenaYtelseVedtakService::createVedtak).collect(Collectors.toList()));

        return trekkResponse;
    }

    private static Vedtak createVedtak(Oppdragsvedtak oppdragsvedtak) {
        Vedtak vedtak = new ObjectFactory().createVedtak();
        vedtak.setVedtaksperiode(oppdragsvedtak.getPeriode());
        vedtak.setDagsats(oppdragsvedtak.getSats());
        vedtak.setTema("DAG");
        vedtak.setRettighetType("DAGO");
        return vedtak;
    }

}
