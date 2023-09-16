package com.example.gatewayservice.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Service
public class ApiStatsService {

    private static final double DECAY_FACTOR = 0.95;
    private static final int WINDOW_SIZE = 100;

    private final Map<String, LinkedList<Double>> latencies = new HashMap<>();
    private final Map<String, LinkedList<Double>> failureRates = new HashMap<>();

    public void recordLatency(String apiName, double latency) {
        latencies.computeIfAbsent(apiName, k -> new LinkedList<>()).addLast(latency);
        maintainWindowSize(latencies.get(apiName));
    }

    public void recordFailureRate(String apiName, double failureRate) {
        failureRates.computeIfAbsent(apiName, k -> new LinkedList<>()).addLast(failureRate);
        maintainWindowSize(failureRates.get(apiName));
    }

    public double getWeightedAverageLatency(String apiName) {
        double weightedAverage = getWeightedAverage(latencies.getOrDefault(apiName, new LinkedList<>()));
        return getWeightedAverage(latencies.getOrDefault(apiName, new LinkedList<>()));
    }

    public double getWeightedAverageFailureRate(String apiName) {
        double weightedAverage = getWeightedAverage(failureRates.getOrDefault(apiName, new LinkedList<>()));

        return getWeightedAverage(failureRates.getOrDefault(apiName, new LinkedList<>()));
    }

    private double getWeightedAverage(LinkedList<Double> data) {
        int size = data.size();
        double weightedSum = 0;
        double weightSum = 0;
        for (int i = 0; i < size; i++) {
            double weight = Math.pow(DECAY_FACTOR, size - i - 1);
            weightedSum += data.get(i) * weight;
            weightSum += weight;
        }
        return weightedSum / weightSum;
    }

    public void clearDataForAPI(String apiName) {
        latencies.remove(apiName);
        failureRates.remove(apiName);
    }

    private void maintainWindowSize(LinkedList<Double> data) {
        while (data.size() > WINDOW_SIZE) {
            data.removeFirst();
        }
    }
}
