package com.griddynamics.pss.repositories.impl;

import com.griddynamics.indexer.services.ProductIndexerService;
import com.griddynamics.pss.models.ProductRequest;
import com.griddynamics.pss.models.ProductResponse;
import com.griddynamics.pss.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    private static final String BRAND_AGG = "BrandRangeAgg";
    private static final String PRICE_SUB_AGG = "PriceSubAgg";
    private static final String NAME_FIELD = "name.";
    private static final String ITEM_COUNT_FIELD = "itemCount";
    private static final String RANK_FIELD = "rank";
    private static final String ID_FIELD = "_id";

    private final ProductIndexerService productIndexerService;
    @Override
    public ProductResponse getProductsByQuery(ProductRequest request) {
        return null;
    }

    @Override
    public void recreateIndex() {
        productIndexerService.recreateIndex();
    }
}
