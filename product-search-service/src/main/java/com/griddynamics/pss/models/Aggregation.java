package com.griddynamics.pss.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Aggregation{
    private String value;
    private Long count;
}
