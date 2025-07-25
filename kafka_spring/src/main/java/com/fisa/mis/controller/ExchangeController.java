package com.fisa.mis.controller;

import com.fisa.mis.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ExchangeController {
    private final ExchangeService exchangeService;

    // 1. Client가 환전 요청
    @PostMapping("/exchange")
    public ResponseEntity<?> exchange(@RequestBody Map<String, Object> req) {
        // (여기선 Kafka로 이벤트만 발행, 결과는 비동기)
        return exchangeService.exchange(req);
    }
}