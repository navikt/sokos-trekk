package no.nav.maskinelletrekk.trekk.ytelsevedtak;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.GregorianCalendar;

public class DateUtil {

    private DateUtil() {
    }

    public static XMLGregorianCalendar toXmlGregorianCalendar(LocalDate fomDato) throws DatatypeConfigurationException {
        GregorianCalendar gcal = GregorianCalendar.from(fomDato.atStartOfDay(ZoneId.systemDefault()));
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
    }

    static LocalDate toLocalDate(XMLGregorianCalendar dato) {
        return dato.toGregorianCalendar().toZonedDateTime().toLocalDate();
    }

}
