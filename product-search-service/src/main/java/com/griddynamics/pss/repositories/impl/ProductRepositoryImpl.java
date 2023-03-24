package com.griddynamics.pss.repositories.impl;

import com.griddynamics.indexer.services.ProductIndexerService;
import com.griddynamics.pss.models.ProductRequest;
import com.griddynamics.pss.models.ProductResponse;
import com.griddynamics.pss.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    private static final String BRAND_AGG = "BrandRangeAgg";
    private static final String PRICE_SUB_AGG = "PriceSubAgg";
    private static final String NAME_FIELD = "name.title";
    private static final String NAME_SHINGLES_FIELD = "name.shingles";
    private static final String BRAND_FIELD = "brand.title";
    private static final String BRAND_SHINGLES_FIELD = "brand.shingles";
    private static final String PRICE_FIELD = "price";
    private static final String SKUS_FIELD = "skus";
    private static final String SKUS_COLOR_FIELD = "skus.color";
    private static final String SKUS_SIZE_FIELD = "skus.size";
    private static final String ID_FIELD = "_id";

    private final RestHighLevelClient client;
    private final ProductIndexerService productIndexerService;

    /*@Value("${com.griddynamics.product.search.es.index}")
    private String indexName;
    @Value("${com.griddynamics.product.search..fuzziness.startsFromLength.one:4}")
    int fuzzyOneStartsFromLength;
    @Value("${com.griddynamics.es.graduation.project.request.fuzziness.startsFromLength.two:6}")
    int fuzzyTwoStartsFromLength;
    @Value("${com.griddynamics.es.graduation.project.request.fuzziness.boost.zero:1.0}")
    float fuzzyZeroBoost;
    @Value("${com.griddynamics.es.graduation.project.request.fuzziness.boost.one:0.5}")
    float fuzzyOneBoost;
    @Value("${com.griddynamics.es.graduation.project.request.fuzziness.boost.two:0.25}")
    float fuzzyTwoBoost;
    @Value("${com.griddynamics.es.graduation.project.request.prefixQueryBoost:0.9}")
    float prefixQueryBoost;*/

    @Override
    public ProductResponse getProductsByQuery(ProductRequest request) {
        return null;
    }

    @Override
    public void recreateIndex() {
        productIndexerService.recreateIndex();
    }
}
