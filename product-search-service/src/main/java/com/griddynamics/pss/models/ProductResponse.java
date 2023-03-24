package com.griddynamics.pss.models;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ProductResponse {
    private Long totalHits;
    private List<Map<String, Object>> products;
    private Map<String, Map<String, Map<String, Number>>> facets = new HashMap<>();
}
