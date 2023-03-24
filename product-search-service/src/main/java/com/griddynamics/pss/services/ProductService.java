package com.griddynamics.pss.services;

import com.griddynamics.pss.models.ProductRequest;
import com.griddynamics.pss.models.ProductResponse;

public interface ProductService {
    ProductResponse getServiceResponse(ProductRequest request);
    void recreateIndex();
}
