package com.example.newsservice.service;

import com.example.newsservice.model.NewsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NewsService {

    @Value("${newsapi.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public NewsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public NewsResponse getNews(String query) {
        String url = String.format("https://newsapi.org/v2/everything?q=%s&apiKey=%s", query, apiKey);
        return restTemplate.getForObject(url, NewsResponse.class);
    }
}
