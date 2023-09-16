package com.example.gatewayservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
public class APIScoreCard {
    private String apiName;
    private APIPerformanceMetrics performanceMetrics;
}

