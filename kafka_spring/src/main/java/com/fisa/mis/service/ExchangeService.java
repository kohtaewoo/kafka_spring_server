package com.fisa.mis.service;

import com.fisa.mis.kafka.ExchangeProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeService {
    private final ExchangeProducer producer;

    public ResponseEntity<?> exchange(Map<String, Object> req) {
        // 2. Kafka에 "환전 요청" 메시지 발행
        producer.sendExchangeRequest(req);
        return ResponseEntity.ok(Map.of("success", true, "message", "환전 요청 송신 완료 (비동기)"));
    }
}
