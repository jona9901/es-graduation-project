package com.griddynamics.indexer.models;

import lombok.Data;

import java.util.List;

@Data
public class Product {
    private int id;
    private String brand;
    // TODO: shingles as derivative field instead of modifying the document
    private List<String> brandShingles;
    private String name;
    private List<String> nameShingles;
    private float price;
    private List<Skus> skus;
}
