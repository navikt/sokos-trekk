package no.nav.maskinelletrekk.trekk.helper;

import no.nav.maskinelletrekk.arenamock.v1.ArenaMockData;
import no.nav.maskinelletrekk.trekk.v1.Trekk;

import javax.xml.bind.JAXB;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class XmlHelper {

    public static Trekk getRequestFromXml(String filename) throws Exception {
        InputStream resourceAsStream = XmlHelper.class.getClassLoader().getResourceAsStream(filename);
        assert resourceAsStream != null;
        return JAXB.unmarshal(resourceAsStream, Trekk.class);
    }

    public static ArenaMockData getMockDataFromXml(String filename) {
        InputStream resourceAsStream = XmlHelper.class.getClassLoader().getResourceAsStream(filename);
        assert resourceAsStream != null;
        return JAXB.unmarshal(resourceAsStream, ArenaMockData.class);
    }

    public static String getRequestFromXmlAsString(String filename) throws Exception {
        Path path = Paths.get(XmlHelper.class.getClassLoader().getResource(filename).toURI());
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

}
