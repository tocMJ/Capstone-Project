package com.example.seekingalphaservice.model;

import lombok.Data;
import java.util.List;

@Data
public class SeekingAlphaResponse {

    private List<DataItem> data;

    @Data
    public static class DataItem {
        private String id;
        private String type;
        private Attributes attributes;
    }

    @Data
    public static class Attributes {
        private String publishOn;
        private String title;
    }
}
