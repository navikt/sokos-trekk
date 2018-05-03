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

    public static XMLGregorianCalendar toXmlGregorianCalendar(LocalDate dato) throws DatatypeConfigurationException {
        XMLGregorianCalendar cal = null;
        if (dato != null) {
            GregorianCalendar gcal = GregorianCalendar.from(dato.atStartOfDay(ZoneId.systemDefault()));
            cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        }
        return cal;
    }

    static LocalDate toLocalDate(XMLGregorianCalendar dato) {
        LocalDate date = null;
        if (dato != null) {
            date = dato.toGregorianCalendar().toZonedDateTime().toLocalDate();
        }
        return date;
    }

}
