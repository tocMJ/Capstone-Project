package com.example.gatewayservice.filters;

import com.example.gatewayservice.service.ApiStatsService;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestTimingStopFilter implements GlobalFilter, Ordered {

    private final ApiStatsService apiStatsService;

    public RequestTimingStopFilter(ApiStatsService apiStatsService) {
        this.apiStatsService = apiStatsService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).doFinally(signalType -> {
            long startTime = exchange.getAttribute("startTime");
            long endTime = System.currentTimeMillis();
            double latency = (endTime - startTime) / 1000.0;
            boolean isSuccess = (exchange.getResponse().getStatusCode().is2xxSuccessful());
            double failureRate = isSuccess ? 0 : 1;
            String bestAPI = exchange.getAttribute("bestAPI");
            if (bestAPI != null) {
                apiStatsService.recordLatency(bestAPI, latency);
                apiStatsService.recordFailureRate(bestAPI, failureRate);
            }
        });
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
