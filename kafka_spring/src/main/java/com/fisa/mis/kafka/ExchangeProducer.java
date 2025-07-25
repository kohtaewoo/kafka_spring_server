package com.fisa.mis.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ExchangeProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String EXCHANGE_TOPIC = "exchange-request";

    public void sendExchangeRequest(Map<String, Object> req) {
        kafkaTemplate.send(EXCHANGE_TOPIC, req);
    }

    // (보상 트랜잭션용 토픽 추가 가능)
    public void sendCompensateRequest(Map<String, Object> req) {
        kafkaTemplate.send("compensate-request", req);
    }
}