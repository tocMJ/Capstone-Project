package com.example.gatewayservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.gatewayservice.service.RequestRateService;

import java.util.Map;
import java.util.HashMap;

@RestController
public class RequestRateController {

    private final RequestRateService requestRateService;

    public RequestRateController(RequestRateService requestRateService) {
        this.requestRateService = requestRateService;
    }

    @GetMapping("/request-rate")
    public Map<String, Object> getRequestRate() {
        Map<String, Object> response = new HashMap<>();
        response.put("requestsPerMinute", requestRateService.getRequestsPerMinute());
        response.put("requestHistory", requestRateService.getRequestHistory());
        return response;
    }
}
