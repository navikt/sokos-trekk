package no.nav.maskinelletrekk.trekk.sikkerhet;

import org.apache.cxf.Bus;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class STSClientWSTrust13and14Test {
    @Test
    public void useSecondaryParameters() throws Exception {
        STSClientWSTrust13and14 stsClient = new STSClientWSTrust13and14(Mockito.mock(Bus.class));
        Assert.assertFalse(stsClient.useSecondaryParameters());
    }
}