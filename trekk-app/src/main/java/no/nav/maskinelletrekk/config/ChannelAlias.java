package no.nav.maskinelletrekk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("TREKK_CHANNEL")
public class ChannelAlias {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ChannelAlias{" +
                "name='" + name + '\'' +
                '}';
    }
}
