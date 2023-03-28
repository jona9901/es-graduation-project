package com.griddynamics.pss.models;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotNull
    @NonNull
    private String queryText;
    private Integer size;
    private Integer page;
}
