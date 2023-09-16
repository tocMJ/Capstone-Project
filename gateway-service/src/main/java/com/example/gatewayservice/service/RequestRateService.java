package com.example.gatewayservice.service;

import com.example.gatewayservice.configuration.IntelligentSelectorProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RequestRateService {

    private final IntelligentSelectorProperties intelligentSelectorProperties;
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final List<Integer> requestHistory = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean timerStarted = false;

    private String currentAPI;

    public RequestRateService(IntelligentSelectorProperties intelligentSelectorProperties) {
        this.intelligentSelectorProperties = intelligentSelectorProperties;
    }

    public void increaseCount() {

        if (!timerStarted) {
            synchronized (this) {
                if (!timerStarted) {
                    startScheduler();
                    timerStarted = true;
                }
            }
        }
    }

    public int getCount() {
        int count = requestCount.get();
        return count;
    }

    private void startScheduler() {

        int interval = intelligentSelectorProperties.getSelectionIntervalInSeconds();
        scheduler.scheduleAtFixedRate(this::resetCount, interval, interval, TimeUnit.SECONDS);
    }

    public synchronized void resetCount() {
        System.out.println("Resetting count...from request rate service");
        requestHistory.add(requestCount.get());
        requestCount.set(0);
    }


    public List<Integer> getRequestHistory() {
        return requestHistory;
    }

    public int getRequestsPerMinute() {
        int count = getCount();
        return count;
    }

    public String getCurrentAPI() {
        return currentAPI;
    }
}
