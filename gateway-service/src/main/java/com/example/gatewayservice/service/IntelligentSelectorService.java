package com.example.gatewayservice.service;

import com.example.gatewayservice.configuration.IntelligentSelectorProperties;
import com.example.gatewayservice.model.APIScoreCard;
import com.example.gatewayservice.model.APIPerformanceMetrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class IntelligentSelectorService {

    private final IntelligentSelectorProperties intelligentSelectorProperties;
    private List<APIScoreCard> apiScoreCards;
    private final RequestRateService requestRateService;
    private final ApiStatsService apiStatsService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean timerStarted = false;

    private Map<String, Double> apiScores = new HashMap<>();
    private String currentBestAPI;


    @PostConstruct
    public void initialize() {

        this.apiScoreCards = intelligentSelectorProperties.getApiScoreCards();
        performIntelligentSelection();
    }

    @Autowired
    public IntelligentSelectorService(
            List<APIScoreCard> apiScoreCards,
            RequestRateService requestRateService,
            ApiStatsService apiStatsService,
            IntelligentSelectorProperties intelligentSelectorProperties
    ) {
        this.apiScoreCards = apiScoreCards;
        this.requestRateService = requestRateService;
        this.apiStatsService = apiStatsService;
        this.intelligentSelectorProperties = intelligentSelectorProperties;
    }

    public void ensureSchedulerStarted() {
        if (!timerStarted) {
            synchronized (this) {
                if (!timerStarted) {
                    int interval = intelligentSelectorProperties.getSelectionIntervalInSeconds();
                    scheduler.scheduleAtFixedRate(this::performIntelligentSelection, interval, interval, TimeUnit.SECONDS);
                    timerStarted = true;
                }
            }
        }
    }

    public void performIntelligentSelection() {
        updateMetricsAndReset();

        calculateScores();
        selectAndSetBestAPI();
    }


    public void calculateScores() {
        int currentRate = requestRateService.getRequestsPerMinute();

        double newsCost = intelligentSelectorProperties.getNews().getCost();

        double weightCost = 0.3;
        double weightQuality = 0.3;
        double weightLatency = 0.3;
        double weightFailureRate = 0.1;

        double maxCost = 100.0;
        double maxQuality = 1.0;
        double maxLatency = 10.0;
        double maxFailureRate = 1.0;

        double minCost = 0.0;
        double minQuality = 0.0;
        double minLatency = 0.0;
        double minFailureRate = 0.0;

        for (APIScoreCard scoreCard : apiScoreCards) {

            String apiName = scoreCard.getApiName();
            APIPerformanceMetrics metrics = intelligentSelectorProperties.getMetricsFor(apiName);


            if (metrics != null) {
                if (currentRate > metrics.getCapacityPerMinute()) {
                    continue;
                }

                double latency = apiStatsService.getWeightedAverageLatency(scoreCard.getApiName());
                double failureRate = apiStatsService.getWeightedAverageFailureRate(scoreCard.getApiName());


                if (Double.isNaN(latency)) {
                    latency = metrics.getLatency();
                }

                if (Double.isNaN(failureRate)) {
                    failureRate = metrics.getFailureRate();
                }

                double scoreCost = (maxCost - metrics.getCost()) / (maxCost - minCost);
                double scoreQuality = (metrics.getQualityOfData() - minQuality) / (maxQuality - minQuality);
                double scoreLatency = (maxLatency - latency) / (maxLatency - minLatency);
                double scoreFailureRate = (maxFailureRate - failureRate) / (maxFailureRate - minFailureRate);

                double score = weightCost * scoreCost + weightQuality * scoreQuality +
                        weightLatency * scoreLatency + weightFailureRate * scoreFailureRate;

                apiScores.put(scoreCard.getApiName(), score);
            }
        }
    }

    public void selectAndSetBestAPI() {


        if (apiScores.isEmpty()) {
            throw new RuntimeException("No APIs to choose from");
        }

        String bestAPI = apiScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (bestAPI == null) {
            System.out.println("No suitable API found, keeping the current one");
        } else {
            currentBestAPI = bestAPI;

            boolean switchAPI = false;
            double apiScore = apiScores.get(bestAPI);

            if (apiScore < intelligentSelectorProperties.getScoreThreshold() || new Random().nextDouble() < intelligentSelectorProperties.getRandomThreshold()) {
                switchAPI = true;
                System.out.println("Switch triggered, selecting a new API...");
            }

            long startTime = System.currentTimeMillis();

            while (switchAPI && System.currentTimeMillis() - startTime < 1000) {
                String newAPI = getRandomAPI();
                double newApiScore = apiScores.getOrDefault(newAPI, 0.0);

                if (newApiScore > intelligentSelectorProperties.getScoreThreshold() && new Random().nextDouble() > intelligentSelectorProperties.getRandomThreshold()) {
                    switchAPI = false;
                    currentBestAPI = newAPI;
                    System.out.println("New API selected: " + currentBestAPI);
                }
            }

            if (switchAPI) {
                throw new RuntimeException("API selection not completed within time limit");
            }
        }
    }

    public void updateMetricsAndReset() {
        APIScoreCard scoreCard = apiScoreCards.stream()
                .filter(card -> card.getApiName().equals(currentBestAPI))
                .findFirst()
                .orElse(null);

        if (scoreCard != null) {
            APIPerformanceMetrics metrics = scoreCard.getPerformanceMetrics();

            double averageLatency = apiStatsService.getWeightedAverageLatency(currentBestAPI);
            if (!Double.isNaN(averageLatency)) {
                metrics.setLatency(averageLatency);
            }
            double averageFailureRate = apiStatsService.getWeightedAverageFailureRate(currentBestAPI);
            if (!Double.isNaN(averageFailureRate)) {
                metrics.setFailureRate(averageFailureRate);
            }
        }
    }



    private String getRandomAPI() {
        int randomIndex = new Random().nextInt(apiScoreCards.size());
        return apiScoreCards.get(randomIndex).getApiName();
    }


    public String getCurrentBestAPI() {
        return currentBestAPI;
    }
}

