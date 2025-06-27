package com.iot.amqp;


public interface AmqpConstants {
    /**
     * AMQP接入域名
     * 参考：https://support.huaweicloud.com/usermanual-iothub/iot_01_00100_2.html#section2
     */
    String HOST = "1ef972c084.st1.iotda-app.cn-north-4.myhuaweicloud.com";   // eg: "****.iot-amqps.cn-north-4.myhuaweicloud.com";

    /**
     * AMQP接入端口
     * 参考：https://support.huaweicloud.com/usermanual-iothub/iot_01_00100_2.html#section2
     */
    int PORT = 5671;

    /**
     * 接入凭证键值
     * 参考：https://support.huaweicloud.com/usermanual-iothub/iot_01_00100_2.html#section3
     */
    String ACCESS_KEY = "JOPvCH6V";

    /**
     * 接入凭证密钥
     * 参考：https://support.huaweicloud.com/usermanual-iothub/iot_01_00100_2.html#section3
     */
    String ACCESS_CODE = "Y94aIdqb3BND7fFm6RqZTDieXn7I2bWE";

    /**
     * 默认队列
     */
    String DEFAULT_QUEUE = "DefaultQueue";
}
