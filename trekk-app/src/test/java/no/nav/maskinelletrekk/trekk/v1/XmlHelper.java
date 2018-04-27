package no.nav.maskinelletrekk.trekk.v1;

import javax.xml.bind.JAXB;
import java.io.FileInputStream;
import java.net.URL;

public class XmlHelper {

    public static Trekk getRequestFromXml(String filename) throws Exception{
        URL resource = XmlHelper.class.getClassLoader().getResource(filename);
        assert resource != null;
        return JAXB.unmarshal(new FileInputStream(resource.getFile()), Trekk.class);
    }

}
