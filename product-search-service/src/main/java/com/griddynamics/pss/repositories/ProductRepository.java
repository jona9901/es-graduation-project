package com.griddynamics.pss.repositories;

import com.griddynamics.pss.models.ProductRequest;
import com.griddynamics.pss.models.ProductResponse;

public interface ProductRepository {
    ProductResponse getProductsByQuery(ProductRequest request);
    void recreateIndex();
}
