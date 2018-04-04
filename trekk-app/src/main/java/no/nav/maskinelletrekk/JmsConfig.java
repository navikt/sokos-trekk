package no.nav.maskinelletrekk;

import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQXAConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import com.ibm.msg.client.wmq.v6.base.internal.MQC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;

@Configuration
public class JmsConfig {

    @Bean
    public Queue trekkInnQueue(@Value("${TREKK_TREKK_INN_QUEUENAME}") String trekkInnQueueName) throws JMSException {
        return new MQQueue(trekkInnQueueName);
    }

    @Bean
    public ConnectionFactory wmqConnectionFactory(GatewayAlias gateway, ChannelAlias channel) throws JMSException {
        MQXAConnectionFactory connectionFactory = new MQXAConnectionFactory();
        connectionFactory.setHostName(gateway.getHostname());
        connectionFactory.setPort(gateway.getPort());
        connectionFactory.setChannel(channel.getName());
        connectionFactory.setQueueManager(gateway.getName());
        connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        connectionFactory.setCCSID(1208);
        connectionFactory.setIntProperty(WMQConstants.JMS_IBM_ENCODING, MQC.MQENC_NATIVE);
        connectionFactory.setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET, 1208);
        return connectionFactory;
    }
}
