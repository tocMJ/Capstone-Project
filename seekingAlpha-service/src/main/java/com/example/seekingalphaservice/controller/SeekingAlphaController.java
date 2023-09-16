package com.example.seekingalphaservice.controller;

import com.example.seekingalphaservice.service.SeekingAlphaService;
import com.example.seekingalphaservice.model.SeekingAlphaResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/autocomplete")
public class SeekingAlphaController {

    private final SeekingAlphaService seekingAlphaService;

    public SeekingAlphaController(SeekingAlphaService seekingAlphaService) {
        this.seekingAlphaService = seekingAlphaService;
    }

    @GetMapping
    public Mono<ResponseEntity<SeekingAlphaResponse>> getArticles(@RequestParam String query) {
        return seekingAlphaService.getArticles(query)
                .map(response -> ResponseEntity.ok(response))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
