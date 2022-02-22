package no.nav.maskinelletrekk.trekk.behandletrekkvedtak;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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