package com.example.bingservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class News {
    private String _type;
    private String readLink;
    private QueryContext queryContext;
    private int totalEstimatedMatches;
    private List<Sort> sort;
    private List<Value> value;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryContext {
        private String originalQuery;
        private boolean adultIntent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sort {
        private String name;
        private String id;
        private boolean isSelected;
        private String url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Value {
        private String name;
        private String url;
        private String description;
    }
}
