package no.nav.maskinelletrekk.trekk.v1.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    @Override
    public LocalDate unmarshal(String localDateAsString) {
        if (localDateAsString == null || localDateAsString.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(localDateAsString.trim(), DateTimeFormatter.ISO_DATE);
    }

    @Override
    public String marshal(LocalDate localDate) {
        return localDate != null ? localDate.format(DateTimeFormatter.ISO_DATE) : null;
    }

}
