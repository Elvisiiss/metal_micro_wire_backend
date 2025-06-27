package com.iot.amqp;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class AmqpClientOptions {
    private String host;
    @Builder.Default
    private int port = 5671;
    private String accessKey;
    private String accessCode;
    private String clientId;
    /**
     * 仅支持true
     */
    @Builder.Default
    private boolean useSsl = true;

    /**
     * IoTDA仅支持default
     */
    @Builder.Default
    private String vhost = "default";

    /**
     * IoTDA仅支持PLAIN
     */
    @Builder.Default
    private String saslMechanisms = "PLAIN";

    /**
     * true: SDK自动ACK（默认）
     * false:收到消息后，需要手动调用message.acknowledge()
     */
    @Builder.Default
    private boolean isAutoAcknowledge = true;

    /**
     * 重连时延（ms）
     */
    @Builder.Default
    private long reconnectDelay = 3000L;

    /**
     * 最大重连时延（ms）,随着重连次数增加重连时延逐渐增加
     */
    @Builder.Default
    private long maxReconnectDelay = 30 * 1000L;

    /**
     * 最大重连次数,默认值-1，代表没有限制
     */
    @Builder.Default
    private long maxReconnectAttempts = -1;

    /**
     * The idle timeout in milliseconds after which the connection will be failed if the peer sends no AMQP frames. Default is 30000.
     */
    @Builder.Default
    private long idleTimeout = 30 * 1000L;

    /**
     * The values below control how many messages the remote peer can send to the client and be held in a pre-fetch buffer for each consumer instance.
     */
    @Builder.Default
    private int queuePrefetch = 1000;

    /**
     * 扩展参数
     */
    private Map<String, String> extendedOptions;

    public String generateConnectUrl() {
        String uri = MessageFormat.format("{0}://{1}:{2}", (useSsl ? "amqps" : "amqp"), host, String.valueOf(port));
        Map<String, String> uriOptions = new HashMap<>();
        uriOptions.put("amqp.vhost", vhost);
        uriOptions.put("amqp.idleTimeout", String.valueOf(idleTimeout));
        uriOptions.put("amqp.saslMechanisms", saslMechanisms);

        Map<String, String> jmsOptions = new HashMap<>();
        jmsOptions.put("jms.prefetchPolicy.queuePrefetch", String.valueOf(queuePrefetch));
        if (StringUtils.isNotEmpty(clientId)) {
            jmsOptions.put("jms.clientID", clientId);
        } else {
            jmsOptions.put("jms.clientID", UUID.randomUUID().toString());
        }
        jmsOptions.put("failover.reconnectDelay", String.valueOf(reconnectDelay));
        jmsOptions.put("failover.maxReconnectDelay", String.valueOf(maxReconnectDelay));
        if (maxReconnectAttempts > 0) {
            jmsOptions.put("failover.maxReconnectAttempts", String.valueOf(maxReconnectAttempts));
        }
        if (extendedOptions != null) {
            for (Map.Entry<String, String> option : extendedOptions.entrySet()) {
                if (option.getKey().startsWith("amqp.") || option.getKey().startsWith("transport.")) {
                    uriOptions.put(option.getKey(), option.getValue());
                } else {
                    jmsOptions.put(option.getKey(), option.getValue());
                }
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(uriOptions.entrySet().stream()
                .map(option -> MessageFormat.format("{0}={1}", option.getKey(), option.getValue()))
                .collect(Collectors.joining("&", "failover:(" + uri + "?", ")")));
        stringBuilder.append(jmsOptions.entrySet().stream()
                .map(option -> MessageFormat.format("{0}={1}", option.getKey(), option.getValue()))
                .collect(Collectors.joining("&", "?", "")));
        return stringBuilder.toString();
    }
}
