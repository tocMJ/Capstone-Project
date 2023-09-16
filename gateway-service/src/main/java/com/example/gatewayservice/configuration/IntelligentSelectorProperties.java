package com.example.gatewayservice.configuration;

import com.example.gatewayservice.model.APIPerformanceMetrics;
import com.example.gatewayservice.model.APIScoreCard;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@RefreshScope
@Component
@ConfigurationProperties(prefix = "service")
public class IntelligentSelectorProperties {

    private double randomThreshold;
    private double scoreThreshold;

    private ApiConfig bing;

    private ApiConfig news;

    private ApiConfig seekingAlpha;

    private int selectionIntervalInSeconds = 60;

    @Data
    public static class ApiConfig {
        private int capacityPerMinute;
        private double cost;
        private double qualityOfData;
    }

    public APIPerformanceMetrics getMetricsFor(String apiName) {
        switch (apiName) {
            case "bing":
                return new APIPerformanceMetrics(bing.getCapacityPerMinute(), bing.getCost(), bing.getQualityOfData(), 0.1, 0.01);
            case "news":
                return new APIPerformanceMetrics(news.getCapacityPerMinute(), news.getCost(), news.getQualityOfData(), 0.15, 0.02);
            case "seekingAlpha":
                return new APIPerformanceMetrics(seekingAlpha.getCapacityPerMinute(), seekingAlpha.getCost(), seekingAlpha.getQualityOfData(), 0.2, 0.03);
            default:
                throw new IllegalArgumentException("Unsupported API: " + apiName);
        }
    }

    public List<APIScoreCard> getApiScoreCards() {
        List<APIScoreCard> cards = new ArrayList<>();
        cards.add(new APIScoreCard("bing", getMetricsFor("bing")));
        cards.add(new APIScoreCard("news", getMetricsFor("news")));
        cards.add(new APIScoreCard("seekingAlpha", getMetricsFor("seekingAlpha")));
        return cards;
    }

}
