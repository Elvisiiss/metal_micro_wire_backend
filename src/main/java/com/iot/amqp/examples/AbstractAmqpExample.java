package com.iot.amqp.examples;


import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public abstract class AbstractAmqpExample {
    private final AtomicLong receiveMessageCount = new AtomicLong();
    private final AtomicLong lastMillis = new AtomicLong();
    private final AtomicLong lastReceiveMessageCount = new AtomicLong();

    /**
     * 开始接收amqp消息
     */
    protected abstract void start();

    /**
     * 停止接收消息
     */
    protected abstract void stop();

    /**
     * 处理消息
     */
    protected void processMessage(String message) {
        log.info("receive a message,content={}", message);
        // todo:用来统计消费速度
        receiveMessageCount.incrementAndGet();
    }

    /**
     * 计算接收速度
     */
    public double calcReceiveSpeed(long currentTimeMillis) {
        if (lastMillis.get() < 1) {
            lastMillis.set(currentTimeMillis);
            lastReceiveMessageCount.set(receiveMessageCount.get());
            return 0;
        } else {
            long useMillis = System.currentTimeMillis() - lastMillis.get();
            long receiveCount = receiveMessageCount.get() - lastReceiveMessageCount.get();
            lastReceiveMessageCount.set(receiveMessageCount.get());
            lastMillis.set(currentTimeMillis);
            log.warn("total receive count:{}", receiveMessageCount.get());
            return (useMillis == 0) ? 0D : (receiveCount * 1000D / useMillis);
        }
    }
}
