package com.griddynamics.indexer.models;

import lombok.Data;

import java.util.List;

@Data
public class Product {
    private int id;
    private String brand;
    private List<String> brand_shingles;
    private String name;
    private List<String> name_shingles;
    private float price;
    private List<Skus> skus;
}
