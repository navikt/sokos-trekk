package no.nav.maskinelletrekk.trekk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateAdapter extends XmlAdapter<String,LocalDate> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalDateAdapter.class);

    @Override
    public LocalDate unmarshal(String localDateAsString) {
        try {
            return LocalDate.parse(localDateAsString, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            LOGGER.error("Parsing av dato '{}' feilet!", localDateAsString);
            return null;
        }
    }

    @Override
    public String marshal(LocalDate localDate) {
        return localDate != null ? localDate.format(DateTimeFormatter.ISO_DATE) : null;
    }
}
