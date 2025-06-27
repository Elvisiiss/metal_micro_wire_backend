package com.iot.amqp.examples;

import com.iot.amqp.AmqpClient;
import com.iot.amqp.AmqpClientOptions;
import com.iot.amqp.AmqpConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;

import lombok.extern.slf4j.Slf4j;

/**
 * 多队列(多租户，每个租户一个或多个队列)情况，主动拉取消息
 * <p>
 * 此种消费方式，如果客户端再收到消息后处理消息速度较慢，会阻塞所有队列的消费。
 * 如果客户端处理消息能力有限，防止服务端推送太多消息导致客户端处理不及时，可采用此消费方式
 * 注意：采用该方式每个队列会申请4~5根线程，如果队列数量过多，可能造成内存溢出，程序崩溃
 */
@Slf4j
public class MultiQueuePullMessage extends AbstractAmqpExample {
    private List<AmqpClient> amqpClientList;

    /**
     * 开启一个线程用来主动拉取消息
     */
    private final ExecutorService executorService = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(1));

    private final AtomicBoolean isClose = new AtomicBoolean();

    @Override
    protected void start() {
        try {
            amqpClientList = createMultiAmqpClient();
        } catch (Exception e) {
            log.warn("createMultiAmqpClient error,", e);
            return;
        }
        Set<MessageConsumer> consumerList = new HashSet<>();
        amqpClientList.forEach(amqpClient -> {
            try {
                // todo:修改为自己的队列。如果该租户存在多个队列，请在此创建多个队列的订阅
                consumerList.add(amqpClient.newConsumer(AmqpConstants.DEFAULT_QUEUE));
            } catch (Exception e) {
                log.warn("create consumer error,", e);
            }
        });
        pullMessageWithMultiConsumer(consumerList);
    }

    @Override
    protected void stop() {
        isClose.set(true);
        executorService.shutdown();
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

    private void pullMessageWithMultiConsumer(Set<MessageConsumer> consumerList) {
        AtomicLong receiveNullMessageCount = new AtomicLong();
        long sleepThreshold = consumerList.size() * 2L + 1;
        // todo:本示例程序采用一个线程拉取所有consumer的消息，可根据自己实际需要自主扩大线程数量
        executorService.execute(() -> {
            while (!isClose.get()) {
                consumerList.forEach(consumer -> {
                    try {
                        Message message = consumer.receiveNoWait();
                        if (message == null) {
                            // 防止cpu占用过高，多次没有拉取到消息后休眠1ms
                            if (receiveNullMessageCount.incrementAndGet() > sleepThreshold) {
                                try {
                                    TimeUnit.MILLISECONDS.sleep(1);
                                } catch (InterruptedException e) {
                                    log.warn("sleep InterruptedException", e);
                                } finally {
                                    receiveNullMessageCount.set(0);
                                }
                            }
                            return;
                        }
                        receiveNullMessageCount.set(0);
                        // 此处进行消息处理。如果处理比较耗时，最好进行开启新的线程处理，否则可能造成心跳超时链接断开。
                        processMessage(message.getBody(String.class));
                        // 如果options.isAutoAcknowledge==false,此处应该调用message.acknowledge();
                    } catch (JMSException e) {
                        log.warn("receive message error,", e);
                    }
                });
            }
        });
    }
}
