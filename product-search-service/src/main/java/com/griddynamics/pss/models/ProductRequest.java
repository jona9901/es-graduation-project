package com.griddynamics.pss.models;

import lombok.Data;

@Data
public class ProductRequest {
    private Integer size;
    private String textQuery;
    private Boolean considerItemCountInSorting;

    public boolean isGetAllRequest() {
        return textQuery == null;
    }
}
