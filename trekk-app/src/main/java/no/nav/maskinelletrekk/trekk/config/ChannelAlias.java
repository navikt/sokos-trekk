package no.nav.maskinelletrekk.trekk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChannelAlias {

    @Value("${TREKK_CHANNEL_NAME}")
    private String name;

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ChannelAlias{" +
                "name='" + name + '\'' +
                '}';
    }
}
