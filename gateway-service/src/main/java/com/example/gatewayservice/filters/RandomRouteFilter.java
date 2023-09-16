package com.example.gatewayservice.filters;

import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Random;

//@Component
public class RandomRouteFilter implements GlobalFilter, Ordered {

    private static final String URI_BING = "http://localhost:8082/news";
    private static final String URI_SEEKING_ALPHA = "http://localhost:8083/autocomplete";
    private static final String URI_News_API = "http://localhost:8084/news";
    private static final Random RAND = new Random();

    private static final String[] URLS = {URI_BING, URI_SEEKING_ALPHA, URI_News_API};

    private final WebClient webClient;

    public RandomRouteFilter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        System.out.println("RandomRouteFilter is created");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("Enter RandomRouteFilter");
        ServerHttpRequest request = exchange.getRequest();

        if (request.getURI().getPath().startsWith("/randomSelector")) {
            String newPath = request.getURI().getPath().replace("/randomSelector", "");
            String query = request.getURI().getQuery();
            String newUri = URLS[RAND.nextInt(URLS.length)] + newPath + (query != null ? "?" + query : "");

            return webClient.method(request.getMethod())
                    .uri(newUri)
                    .headers(headers -> headers.addAll(request.getHeaders()))
                    .exchange()
                    .flatMap(response -> {
                        System.out.println("Response received: " + response.statusCode());
                        exchange.getResponse().setStatusCode(response.statusCode());
                        return exchange.getResponse().writeWith(response.bodyToFlux(DataBuffer.class));
                    });
        }

        System.out.println("Exit RandomRouteFilter");
        return chain.filter(exchange);
    }


    @Override
    public int getOrder() {
        return 1;
    }
}
