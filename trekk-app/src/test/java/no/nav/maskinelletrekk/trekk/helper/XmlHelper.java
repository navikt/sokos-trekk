package no.nav.maskinelletrekk.trekk.helper;

import no.nav.maskinelletrekk.trekk.v1.Trekk;

import javax.xml.bind.JAXB;
import java.io.InputStream;

public class XmlHelper {

    public static Trekk getRequestFromXml(String filename) throws Exception {
        InputStream resourceAsStream = XmlHelper.class.getClassLoader().getResourceAsStream(filename);
        assert resourceAsStream != null;
        return JAXB.unmarshal(resourceAsStream, Trekk.class);
    }

}
