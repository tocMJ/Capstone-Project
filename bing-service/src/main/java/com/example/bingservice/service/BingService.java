package com.example.bingservice.service;

import com.example.bingservice.model.News;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class BingService {

    private static final String BING_NEWS_API_URL = "https://api.bing.microsoft.com/v7.0/news/search";

    @Value("${news.key}")
    private String apiKey;

    private final WebClient webClient;

    public BingService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(BING_NEWS_API_URL).build();
    }

    public Mono<News> getNews(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.queryParam("q", query).build())
                .header("Ocp-Apim-Subscription-Key", apiKey)
                .retrieve()
                .bodyToMono(News.class);
    }
}
