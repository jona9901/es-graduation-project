package com.griddynamics.pss.models;

import lombok.Data;

@Data
public class ProductRequest {
    private String queryText;
    private Integer size;
    private Integer page;
}
