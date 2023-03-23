package com.griddynamics.indexer.models;

import lombok.Data;

import java.util.List;

@Data
public class Product {
    private int id;
    private String brand;
    private String name;
    private float price;
    private List<Skus> skus;
}
