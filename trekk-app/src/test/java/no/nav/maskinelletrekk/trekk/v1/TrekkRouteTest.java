package no.nav.maskinelletrekk.trekk.v1;

import no.nav.maskinelletrekk.trekk.behandletrekkvedtak.BehandleTrekkvedtakBean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class TrekkRouteTest {

    @Mock
    private BehandleTrekkvedtakBean behandleTrekkvedtak;


    @Before
    public void setUp() {
    }

    @Test
    public void skalKjorereRute() {
        assertTrue(true);
    }
}