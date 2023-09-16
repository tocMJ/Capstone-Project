package com.example.gatewayservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
public class APIPerformanceMetrics {
      private int capacityPerMinute;
      private double cost;
      private double qualityOfData;
      private double latency;
      private double failureRate;
}
