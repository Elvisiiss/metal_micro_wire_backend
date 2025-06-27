package com.iot.amqp;


import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.jms.JmsConnection;
import org.apache.qpid.jms.JmsConnectionExtensions;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.JmsQueue;
import org.apache.qpid.jms.transports.TransportOptions;
import org.apache.qpid.jms.transports.TransportSupport;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class AmqpClient {
    private final AmqpClientOptions options;
    private Connection connection;
    private Session session;
    private final Set<MessageConsumer> consumerSet = Collections.synchronizedSet(new HashSet<>());

    public AmqpClient(AmqpClientOptions options) {
        this.options = options;
    }

    public String getId() {
        return options.getClientId();
    }

    public void initialize() throws Exception {
        initialize(null);
    }

    public void initialize(String connectionId) throws Exception {
        String connectionUrl = options.generateConnectUrl();
        if (connectionId != null) {
            connectionUrl += "&jms.connectionIDPrefix=" + connectionId;
        }
        log.info("connectionUrl={}", connectionUrl);
        JmsConnectionFactory cf = new JmsConnectionFactory(connectionUrl);
        // 信任服务端
        TransportOptions to = new TransportOptions();
        to.setTrustAll(true);
        cf.setSslContext(TransportSupport.createJdkSslContext(to));
        String userName = "accessKey=" + options.getAccessKey();
        cf.setExtension(JmsConnectionExtensions.USERNAME_OVERRIDE.toString(), (connection, uri) -> {
            // IoTDA的userName组成格式如下：“accessKey=${accessKey}|timestamp=${timestamp}”
            String newUserName = userName;
            if (connection instanceof JmsConnection) {
                newUserName = ((JmsConnection) connection).getUsername();
            }
            return newUserName + "|timestamp=" + System.currentTimeMillis();
        });
        // 创建连接
        connection = cf.createConnection(userName, options.getAccessCode());
        // 创建 Session, Session.CLIENT_ACKNOWLEDGE: 收到消息后，需要手动调用message.acknowledge()。Session.AUTO_ACKNOWLEDGE: SDK自动ACK（推荐）。
        session = connection.createSession(false, options.isAutoAcknowledge() ? Session.AUTO_ACKNOWLEDGE : Session.CLIENT_ACKNOWLEDGE);
        connection.start();
    }

    public MessageConsumer newConsumer(String queueName) throws Exception {
        if (connection == null || !(connection instanceof JmsConnection) || ((JmsConnection) connection).isClosed()) {
            throw new Exception("create consumer failed,the connection is disconnected.");
        }
        MessageConsumer consumer;

        consumer = session.createConsumer(new JmsQueue(queueName));
        if (consumer != null) {
            consumerSet.add(consumer);
        }
        return consumer;
    }

    public void close() {
        consumerSet.forEach(consumer -> {
            try {
                consumer.close();
            } catch (JMSException e) {
                log.warn("consumer close error,exception is ", e);
            }
        });

        if (session != null) {
            try {
                session.close();
            } catch (JMSException e) {
                log.warn("session close error,exception is ", e);
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                log.warn("connection close error,exception is", e);
            }
        }
    }
}
