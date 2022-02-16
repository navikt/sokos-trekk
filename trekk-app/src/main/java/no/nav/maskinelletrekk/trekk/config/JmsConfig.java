package no.nav.maskinelletrekk.trekk.config;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQXAConnectionFactory;
import com.ibm.msg.client.jms.JmsConstants;
import com.ibm.msg.client.wmq.WMQConstants;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.component.jms.JmsEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;

@Configuration
public class JmsConfig {

    @Value("${trekk.jms.concurrentConsumers}")
    private Integer concurrentConsumers = 1;

    @Value("${trekk.jms.transactionTimeout}")
    private Integer transactionTimeout = 300;

    @Value("${SRVTREKK_USERNAME}")
    private String qmUsername;

    @Value("${SRVTREKK_PASSWORD}")
    private String qmPassword;

    @Value("${TREKK_TREKK_INN_QUEUENAME}")
    private String trekkInnQueueName;

    @Value("${TREKK_TREKK_INN_BOQ_QUEUENAME}")
    private String trekkInnBoqQueueName;

    @Value("${OB04_TREKK_REPLY_QUEUENAME}")
    private String trekkReplyQueueName;

    @Value("${OB04_TREKK_REPLY_BATCH_QUEUENAME}")
    private String trekkReplyBatchQueueName;

    @Bean
    public Queue trekkInnQueue() throws JMSException {
        return new MQQueue(trekkInnQueueName);
    }

    @Bean
    public Queue trekkInnBoqQueue() throws JMSException {
        return new MQQueue(trekkInnBoqQueueName);
    }

    @Bean
    public Queue trekkReplyQueue() throws JMSException {
        MQQueue mqQueue = new MQQueue(trekkReplyQueueName);
        mqQueue.setTargetClient(1);
        return mqQueue;
    }

    @Bean
    public Queue trekkReplyBatchQueue() throws JMSException {
        MQQueue mqQueue = new MQQueue(trekkReplyBatchQueueName);
        mqQueue.setTargetClient(1);
        return mqQueue;
    }

    @Bean("trekkInn")
    public JmsEndpoint trekkInnEndpoint(Queue trekkInnQueue,
                                        JmsConfiguration jmsConfiguration) throws JMSException {
        JmsEndpoint jmsEndpoint = JmsEndpoint.newInstance(trekkInnQueue);
        jmsEndpoint.setConfiguration(jmsConfiguration);
        return jmsEndpoint;
    }

    @Bean("trekkInnBoq")
    public JmsEndpoint trekkInnBoqEndpoint(Queue trekkInnBoqQueue,
                                        JmsConfiguration jmsConfiguration) throws JMSException {
        JmsEndpoint jmsEndpoint = JmsEndpoint.newInstance(trekkInnBoqQueue);
        jmsEndpoint.setConfiguration(jmsConfiguration);
        return jmsEndpoint;
    }

    @Bean("trekkReply")
    public JmsEndpoint trekkReplyEndpoint(Queue trekkReplyQueue,
                                          JmsConfiguration jmsConfiguration) throws JMSException {
        JmsEndpoint jmsEndpoint = JmsEndpoint.newInstance(trekkReplyQueue);
        jmsEndpoint.setConfiguration(jmsConfiguration);
        return jmsEndpoint;
    }

    @Bean("trekkReplyBatch")
    public JmsEndpoint trekkReplyBatchEndpoint(Queue trekkReplyBatchQueue,
                                          JmsConfiguration jmsConfiguration) throws JMSException {
        JmsEndpoint jmsEndpoint = JmsEndpoint.newInstance(trekkReplyBatchQueue);
        jmsEndpoint.setConfiguration(jmsConfiguration);
        return jmsEndpoint;
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
        connectionFactory.setIntProperty(WMQConstants.JMS_IBM_ENCODING, MQConstants.MQENC_NATIVE);
        connectionFactory.setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET, 1208);
        connectionFactory.setBooleanProperty(JmsConstants.USER_AUTHENTICATION_MQCSP, true);

        UserCredentialsConnectionFactoryAdapter adapter = new UserCredentialsConnectionFactoryAdapter();
        adapter.setTargetConnectionFactory(new CachingConnectionFactory(connectionFactory));
        adapter.setUsername(qmUsername);
        adapter.setPassword(qmPassword);

        return adapter;
    }

    @Bean
    public JmsConfiguration jmsConfiguration(ConnectionFactory connectionFactory) {
        JmsConfiguration jmsConfiguration = new JmsConfiguration();
        jmsConfiguration.setConnectionFactory(connectionFactory);
        jmsConfiguration.setTransacted(true);
        jmsConfiguration.setTransactionTimeout(transactionTimeout);
        jmsConfiguration.setConcurrentConsumers(concurrentConsumers);

        return jmsConfiguration;
    }
}
