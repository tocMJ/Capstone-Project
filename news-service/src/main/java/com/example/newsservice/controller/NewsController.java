package com.example.newsservice.controller;

import com.example.newsservice.model.NewsResponse;
import com.example.newsservice.service.NewsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping("/news")
    public NewsResponse getNews(@RequestParam String query) {
        NewsResponse response = newsService.getNews(query);
        if (response.getArticles().size() > 5) {
            response.setArticles(response.getArticles().subList(0, 5));
        }
        return response;
    }

}
