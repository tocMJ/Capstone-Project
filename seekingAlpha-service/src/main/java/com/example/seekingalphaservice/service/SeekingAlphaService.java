package com.example.seekingalphaservice.service;

import com.example.seekingalphaservice.model.SeekingAlphaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Service
public class SeekingAlphaService {

    private static final String SEEKING_ALPHA_API_URL = "https://seeking-alpha.p.rapidapi.com/news/v2/list-by-symbol";

    @Value("${seekingAlpha.key}")
    private String apiKey;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(SEEKING_ALPHA_API_URL)
                .defaultHeader("x-rapidapi-key", apiKey)
                .defaultHeader("x-rapidapi-host", "seeking-alpha.p.rapidapi.com")
                .build();
    }

    public Mono<SeekingAlphaResponse> getArticles(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("id", query)
                        .build())
                .retrieve()
                .bodyToMono(SeekingAlphaResponse.class);
    }
}
