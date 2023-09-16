package com.example.gatewayservice.filters;

import com.example.gatewayservice.service.IntelligentSelectorService;
import com.example.gatewayservice.service.RequestRateService;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.http.server.reactive.ServerHttpRequest;

//@Component
public class IntelligentRouteFilter implements GlobalFilter, Ordered {

    private final IntelligentSelectorService intelligentSelectorService;
    private final RouteDefinitionLocator routeDefinitionLocator;
    private final RequestRateService requestRateService;
    private final AtomicBoolean isSchedulerStarted = new AtomicBoolean(false);

    public IntelligentRouteFilter(IntelligentSelectorService intelligentSelectorService, RouteDefinitionLocator routeDefinitionLocator, RequestRateService requestRateService) {
        this.intelligentSelectorService = intelligentSelectorService;
        this.routeDefinitionLocator = routeDefinitionLocator;
        this.requestRateService = requestRateService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        if (!isSchedulerStarted.getAndSet(true)) {
            intelligentSelectorService.ensureSchedulerStarted();
        }
        ServerHttpRequest request = exchange.getRequest();

        if (request.getURI().getPath().startsWith("/intelligentSelector")) {
            String bestAPI = intelligentSelectorService.getCurrentBestAPI();
            if (bestAPI == null) {
                throw new RuntimeException("No suitable API found");
            }

            requestRateService.increaseCount();

            exchange.getAttributes().put("bestAPI", bestAPI);



            return routeDefinitionLocator.getRouteDefinitions()
                    .filter(routeDefinition -> routeDefinition.getId().equals(bestAPI))
                    .next()
                    .flatMap(routeDefinition -> {
                        System.out.println(routeDefinition);
                        String apiBasePath = routeDefinition.getPredicates().get(0).getArgs().get("_genkey_0").replace("/**", "");
                        URI apiUri = routeDefinition.getUri();

                        String newPath = request.getURI().getPath().replace("/intelligentSelector", "");
                        String query = request.getURI().getQuery();

                        URI newUri = URI.create(apiUri.toString() + apiBasePath + newPath + (query != null ? "?" + query : ""));

                        System.out.println("New URI: " + newUri);


                        ServerHttpRequest newRequest = request.mutate().uri(newUri).build();

                        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

                        return chain.filter(newExchange);
                    });
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
