package com.example.gatewayservice.controller;

import com.example.gatewayservice.service.ApiStatsService;
import com.example.gatewayservice.service.IntelligentSelectorService;
import com.example.gatewayservice.service.RequestRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RefreshScope
public class IntelligentSelectorController {

    private final IntelligentSelectorService intelligentSelectorService;
    private final RequestRateService requestRateService;
    private final ApiStatsService apiStatsService;
    private final AtomicBoolean isSchedulerStarted = new AtomicBoolean(false);

    @Value("${successRate.default}")
    private double defaultSuccessRate;

    @Value("${successRate.bing}")
    private double bingSuccessRate;

    @Value("${successRate.seekingAlpha}")
    private double seekingAlphaSuccessRate;

    @Value("${successRate.news}")
    private double newsSuccessRate;

    private final Map<String, Double> apiSuccessRates = new HashMap<>();


    @Autowired
    public IntelligentSelectorController(IntelligentSelectorService intelligentSelectorService, RequestRateService requestRateService, ApiStatsService apiStatsService) {
        this.intelligentSelectorService = intelligentSelectorService;
        this.requestRateService = requestRateService;
        this.apiStatsService = apiStatsService;
    }

    @PostConstruct
    public void init() {
        apiSuccessRates.put("bing", bingSuccessRate);
        apiSuccessRates.put("seekingAlpha", seekingAlphaSuccessRate);
        apiSuccessRates.put("news", newsSuccessRate);
    }

    @GetMapping("/intelligentSelector")
    public ResponseEntity<String> handleRequest(@RequestParam String query) {
        long startTime = System.currentTimeMillis();

        if (!isSchedulerStarted.getAndSet(true)) {
            intelligentSelectorService.ensureSchedulerStarted();
        }

        requestRateService.increaseCount();
        String currentBestAPI = intelligentSelectorService.getCurrentBestAPI();
        double currentSuccessRate = apiSuccessRates.getOrDefault(currentBestAPI, defaultSuccessRate);

        boolean isSuccess;
        if (new Random().nextDouble() <= currentSuccessRate) {
            isSuccess = true;
        } else {
            isSuccess = false;
        }

        long endTime = System.currentTimeMillis();
        double latency = (endTime - startTime) / 1000.0;
        double failureRate = isSuccess ? 0 : 1;

        apiStatsService.recordLatency(currentBestAPI, latency);
        apiStatsService.recordFailureRate(currentBestAPI, failureRate);


        if (isSuccess) {
            return ResponseEntity.ok("Request processed successfully using API: " + currentBestAPI);
        } else {
            return ResponseEntity.status(400).body("Request failed using API: " + currentBestAPI);
        }
    }

}

