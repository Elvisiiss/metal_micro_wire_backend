package com.iot.amqp.examples;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Slf4j
public class AmqpClientTest {
    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {
        List<AbstractAmqpExample> amqpExamples = new ArrayList<>();
        // 本程序提供5中amqp消费示例，请结合自己的使用场景选择合适的示例
        // 本程序提供的amqp客户端包含断线重连逻辑，不需要额外处理

        // 1、单队列通过设置listener消费amqp消息
        AbstractAmqpExample receiveMessageByListener = new ReceiveMessageByListener();
        receiveMessageByListener.start();
        amqpExamples.add(receiveMessageByListener);

        // 2、单队列主动拉取amqp消息
        // AbstractAmqpExample pullMessage = new PullMessage();
        // pullMessage.start();
        // amqpExamples.add(pullMessage);

        // 3、一个客户端订阅多个队列,通过设置listener消费amqp消息
        // AbstractAmqpExample multiQueueOneClientReceiveMessageByListener = new MultiQueueOneClientReceiveMessageByListener();
        // multiQueueOneClientReceiveMessageByListener.start();
        // amqpExamples.add(multiQueueOneClientReceiveMessageByListener);

        // 4、多个客户端，通过设置listener消费amqp消息
        // AbstractAmqpExample multiQueueReceiveMessageByListener = new MultiQueueReceiveMessageByListener();
        // multiQueueReceiveMessageByListener.start();
        // amqpExamples.add(multiQueueReceiveMessageByListener);

        // 5、多个客户端，主动拉取amqp消息
        // AbstractAmqpExample multiQueuePullMessage = new MultiQueuePullMessage();
        // multiQueuePullMessage.start();
        // amqpExamples.add(multiQueuePullMessage);

        // 计算接收速度
        executorService.scheduleAtFixedRate(() -> {
            long currentTimeMillis = System.currentTimeMillis();
            log.info("Receive message speed");
            amqpExamples.forEach(abstractAmqpExample -> {
                log.info("{}:{} tps", abstractAmqpExample.getClass().getSimpleName(), abstractAmqpExample.calcReceiveSpeed(currentTimeMillis));
            });
        }, 0, 10, TimeUnit.SECONDS);

        // todo:程序退出时，请关闭amqp客户端
        // amqpExamples.forEach(AbstractAmqpExample::stop);
    }
}
