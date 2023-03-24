package com.griddynamics.pss.services.impl;

import com.griddynamics.pss.models.ProductRequest;
import com.griddynamics.pss.models.ProductResponse;
import com.griddynamics.pss.repositories.ProductRepository;
import com.griddynamics.pss.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    @Value("${com.griddynamics.product.search.request.default.findByQuery.size}")
    private int defaultFindByQuerySize;
    @Value("${com.griddynamics.product.search.request.default.findByQuery.page}")
    private int findByQueryPage;
    @Value("${com.griddynamics.product.search.request.default.getAllSize}")
    private int defaultGetAllSize;
    @Value("${com.griddynamics.product.search.request.default.minQueryLength}")
    private int minQueryLength;

    private final ProductRepository productRepository;

    @Override
    public ProductResponse getServiceResponse(ProductRequest request) {
        prepareServiceRequest(request);
        if (request.getQueryText().length() < minQueryLength) {
            return new ProductResponse();
        } else {
            return productRepository.getProductsByQuery(request);
        }
    }

    private void prepareServiceRequest(ProductRequest request) {
        if (request.getSize() == null || request.getSize() <= 0) {
            request.setSize(defaultFindByQuerySize);
        }
    }

    @Override
    public void recreateIndex() {
        productRepository.recreateIndex();
    }
}
