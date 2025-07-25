package com.fisa.mis.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeConsumer {

    private final RestTemplate restTemplate; // Bean 등록 필요!
    private final ExchangeProducer producer; // 보상 트랜잭션 발행용

    @KafkaListener(topics = "exchange-request", groupId = "orchestrator-group")
    public void consumeExchangeRequest(Map<String, Object> req) {
        String id = (String) req.get("id");
        double amount = Double.parseDouble(req.get("amount").toString());

        // 1. 출금 (KRW 서버, HTTP)
        var withdrawRes = restTemplate.postForEntity(
            "http://localhost:8082/withdraw", // 오라클 서버
            Map.of("id", id, "amount", amount),
            Map.class
        );
        if (!(Boolean) withdrawRes.getBody().get("success")) {
            log.info("출금 실패, 환전 실패 처리");
            return;
        }

        // 2. 입금 (USD 서버, HTTP)
        var depositRes = restTemplate.postForEntity(
            "http://localhost:8081/deposit", // MySQL 서버
            Map.of("id", id, "amount", amount / 1300),
            Map.class
        );
        if (!(Boolean) depositRes.getBody().get("success")) {
            // 3. 보상(출금 취소) - 메시지 방식(권장), 또는 즉시 HTTP로 해도 OK
            log.info("입금 실패! 출금 취소(보상) 트리거");
            producer.sendCompensateRequest(Map.of("id", id, "amount", amount));
            return;
        }
        log.info("환전 성공!");
    }

    // 4. 보상 트랜잭션 Consumer (별도 토픽 구독)
    @KafkaListener(topics = "compensate-request", groupId = "orchestrator-group")
    public void compensate(Map<String, Object> req) {
        String id = (String) req.get("id");
        double amount = Double.parseDouble(req.get("amount").toString());

        // 5. 보상 트랜잭션 실제 실행 (출금 취소 HTTP)
        restTemplate.postForEntity(
            "http://localhost:8082/compensate",
            Map.of("id", id, "amount", amount),
            Map.class
        );
        log.info("보상 트랜잭션 실행(출금 취소)");
    }
}