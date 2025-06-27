package com.iot.amqp.examples;

import com.iot.amqp.AmqpClient;
import com.iot.amqp.AmqpClientOptions;
import com.iot.amqp.AmqpConstants;
import lombok.extern.slf4j.Slf4j;

import javax.jms.MessageConsumer;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个客户端可以订阅多个(≤10)队列，同时拉取多个队列中的数据。
 * 此程序演示：一个客户端订阅多个队列情况下通过设置listener消费amqp消息
 */
@Slf4j
public class MultiQueueOneClientReceiveMessageByListener extends AbstractAmqpExample {
    private AmqpClient amqpClient;

    @Override
    protected void start() {
        try {
            amqpClient = createAmqpClient();
            // todo:一个客户端最多支持订阅10个队列
            List<String> queueNameList = new ArrayList<>();
            queueNameList.add(AmqpConstants.DEFAULT_QUEUE);
            // queueNameList.add("queue1");
            // queueNameList.add("queue2");
            queueNameList.forEach(queueName -> {
                try {
                    MessageConsumer consumer = amqpClient.newConsumer(queueName);
                    consumer.setMessageListener(message -> {
                        try {
                            // 此处进行消息处理。如果处理比较耗时，最好进行开启新的线程处理，否则可能造成心跳超时链接断开。
                            processMessage(message.getBody(String.class));
                            // 如果options.isAutoAcknowledge==false,此处应该调用message.acknowledge();
                        } catch (Exception e) {
                            log.warn("message.getBody error,exception is ", e);
                        }
                    });
                } catch (Exception e) {
                    log.warn("create consumer error,", e);
                }
            });
        } catch (Exception e) {
            log.warn("create amqpClient error,", e);
        }
    }

    @Override
    protected void stop() {
        if (amqpClient != null) {
            amqpClient.close();
        }
    }

    /**
     * 创建单个amqpClient，实际使用中根据情况修改参数
     */
    private AmqpClient createAmqpClient() throws Exception {
        AmqpClientOptions options = AmqpClientOptions.builder()
                .host(AmqpConstants.HOST)
                .port(AmqpConstants.PORT)
                .accessKey(AmqpConstants.ACCESS_KEY)
                .accessCode(AmqpConstants.ACCESS_CODE)
                .queuePrefetch(100) // sdk会在内存中分配该参数大小的队列，用来接收消息，客户端内存较小的情况可以调小该参数。
                .build();
        AmqpClient amqpClient = new AmqpClient(options);
        amqpClient.initialize();
        return amqpClient;
    }
}
