package com.example.gatewayservice.controller;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

@RestController
@RequestMapping("/nacos-test")
public class NacosTestController {

    private ConfigService configService;
    private String dataId = "config.properties";
    private String group = "DEFAULT_GROUP";

    public NacosTestController() {
        try {
            String serverAddr = "127.0.0.1:8848";
            configService = NacosFactory.createConfigService(serverAddr);
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/config")
    public ResponseEntity<String> getConfig() {
        String successRate = loadConfig();
        return ResponseEntity.ok("SuccessRate: " + successRate);
    }

    private String loadConfig() {
        try {
            String config = configService.getConfig(dataId, group, 5000);
            Properties properties = new Properties();
            properties.load(new StringReader(config));
            String successRate = properties.getProperty("successRate");
            return successRate;
        } catch (NacosException e) {
            e.printStackTrace();
            return "Failed to load config";
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to parse config";
        }
    }
}

