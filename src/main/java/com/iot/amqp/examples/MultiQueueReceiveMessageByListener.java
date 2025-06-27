package com.iot.amqp.examples;

import com.iot.amqp.AmqpClient;
import com.iot.amqp.AmqpClientOptions;
import com.iot.amqp.AmqpConstants;

import lombok.extern.slf4j.Slf4j;

import javax.jms.MessageConsumer;

import java.util.ArrayList;
import java.util.List;
// import java.util.concurrent.TimeUnit;

/**
 * 多队列(多租户，每个租户有一个或多个队列)情况通过设置listener消费amqp消息
 * <p>
 * 此种消费方式，如果客户端再收到消息后处理消息速度较慢，只会阻塞当前队列的消费，其他队列的消费速度不受影响。
 * 多租户或者单租户多队列情况建议采用该方式。
 * 注意：采用该方式每个队列会申请4~5根线程，如果队列数量过多，可能造成内存溢出，程序崩溃
 */
@Slf4j
public class MultiQueueReceiveMessageByListener extends AbstractAmqpExample {
    private List<AmqpClient> amqpClientList;

    @Override
    protected void start() {
        try {
            amqpClientList = createMultiAmqpClient();
        } catch (Exception e) {
            log.warn("createMultiAmqpClient error,", e);
            return;
        }

        amqpClientList.forEach(amqpClient -> {
            try {
                // todo:修改为自己的队列。如果该租户存在多个队列，请在此创建多个队列的订阅
                MessageConsumer consumer = amqpClient.newConsumer(AmqpConstants.DEFAULT_QUEUE);
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
    }

    @Override
    protected void stop() {
        if (amqpClientList != null) {
            amqpClientList.forEach(AmqpClient::close);
        }
    }

    /**
     * 创建多个amqpClient，实际使用中根据情况修改参数
     */
    private static List<AmqpClient> createMultiAmqpClient() throws Exception {
        List<AmqpClient> amqpClientList = new ArrayList<>();
        // todo:此处创建了5个客户端，实际使用中根据情况修改
        for (int i = 0; i < 5; i++) {
            AmqpClientOptions options1 = AmqpClientOptions.builder()
                .host(AmqpConstants.HOST)
                .port(AmqpConstants.PORT)
                .accessKey(AmqpConstants.ACCESS_KEY)
                .accessCode(AmqpConstants.ACCESS_CODE)
                .queuePrefetch(100) // sdk会在内存中分配该参数大小的队列，用来接收消息，客户端内存较小的情况可以调小该参数。
                .build();
            AmqpClient amqpClient = new AmqpClient(options1);
            amqpClient.initialize();
            amqpClientList.add(amqpClient);
        }
        return amqpClientList;
    }
}
