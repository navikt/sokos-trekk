package no.nav.maskinelletrekk.trekk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GatewayAlias {

    @Value("${MQGATEWAY01_HOSTNAME}")
    private String hostname;
    @Value("${MQGATEWAY01_NAME}")
    private String name;
    @Value("${MQGATEWAY01_PORT}")
    private String port;

    public String getHostname() {
        return hostname;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return Integer.parseInt(port);
    }


    @Override
    public String toString() {
        return "GatewayAlias{" +
                "hostname='" + hostname + '\'' +
                ", name='" + name + '\'' +
                ", port=" + port +
                '}';
    }
}
