package com.example.bingservice.controller;

import com.example.bingservice.model.News;
import com.example.bingservice.service.BingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class BingController {

    private final BingService bingService;

    public BingController(BingService bingService) {
        this.bingService = bingService;
    }

    @GetMapping("/news/{query}")
    public Mono<ResponseEntity<News>> getNews(@PathVariable String query) {
        return bingService.getNews(query)
                .map(news -> ResponseEntity.ok(news))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
