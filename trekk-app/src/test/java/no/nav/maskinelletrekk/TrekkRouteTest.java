package no.nav.maskinelletrekk;

import no.nav.maskinelletrekk.behandletrekkvedtak.BehandleTrekkvedtakBean;
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