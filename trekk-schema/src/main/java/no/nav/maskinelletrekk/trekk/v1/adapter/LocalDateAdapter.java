package no.nav.maskinelletrekk.trekk.v1.adapter;

import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends XmlAdapter<String,LocalDate> {

    @Override
    public LocalDate unmarshal(String localDateAsString) {
        if (StringUtils.isBlank(localDateAsString)) {
            return null;
        }
        return LocalDate.parse(localDateAsString.trim(), DateTimeFormatter.ISO_DATE);
    }

    @Override
    public String marshal(LocalDate localDate) {
        return localDate != null ? localDate.format(DateTimeFormatter.ISO_DATE) : null;
    }
}
