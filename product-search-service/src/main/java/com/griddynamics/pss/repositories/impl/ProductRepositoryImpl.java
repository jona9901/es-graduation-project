package com.griddynamics.pss.repositories.impl;

import com.griddynamics.indexer.services.ProductIndexerService;
import com.griddynamics.pss.models.ProductRequest;
import com.griddynamics.pss.models.ProductResponse;
import com.griddynamics.pss.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.ParsedRange;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregator;
import org.elasticsearch.search.aggregations.metrics.ValueCountAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    private static final String PRICE_AGG = "PriceRangeAgg";
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

    @Value("${com.griddynamics.es.index}")
    private String indexName;
    @Value("${com.griddynamics.product.search.request.fuzziness.startsFromLength.one}")
    int fuzzyOneStartsFromLength;
    @Value("${com.griddynamics.product.search.request.fuzziness.startsFromLength.two}")
    int fuzzyTwoStartsFromLength;
    @Value("${com.griddynamics.product.search.request.fuzziness.boost.zero}")
    float fuzzyZeroBoost;
    @Value("${com.griddynamics.product.search.request.fuzziness.boost.one}")
    float fuzzyOneBoost;
    @Value("${com.griddynamics.product.search.request.fuzziness.boost.two}")
    float fuzzyTwoBoost;
    @Value("${com.griddynamics.product.search.request.prefixQueryBoost}")
    float prefixQueryBoost;

    @Override
    public ProductResponse getProductsByQuery(ProductRequest request) {
        if (request.getQueryText() != null) {
            QueryBuilder mainQuery = getQueryByText(request.getQueryText());
            return getProducts(mainQuery, request);
        } else {
            return new ProductResponse();
        }
    }

    private ProductResponse getProducts(QueryBuilder mainQuery, ProductRequest request) {
        // Create search request
        SearchSourceBuilder ssb = new SearchSourceBuilder()
                .query(mainQuery)
                .size(request.getSize());

        // Add sorting and aggregation
        ssb.sort(new ScoreSortBuilder().order(SortOrder.DESC)); // sort by _score DESC
        ssb.sort(new FieldSortBuilder(ID_FIELD).order(SortOrder.DESC)); // tie breaker: sort by _id DESC

        // Aggregation
        List<AggregationBuilder> aggs = createAggs();
        aggs.forEach(ssb::aggregation);


        // Search in ES
        SearchRequest searchRequest = new SearchRequest(indexName).source(ssb);
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            // Build service response
            return getServiceResponse(searchResponse);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return new ProductResponse();
        }
    }

    private List<AggregationBuilder> createAggs() {
        List<AggregationBuilder> result = new ArrayList<>();

        // Facets: count aggregation by brand count
        /*ValueCountAggregationBuilder brandBuilder = AggregationBuilders
                .count(BRAND_FIELD);*/
        // Facets: range aggregation by price
        RangeAggregationBuilder priceRangeBuilder = AggregationBuilders
                .range(PRICE_AGG)
                .field(PRICE_FIELD)
                .keyed(true)
                .addRange(new RangeAggregator.Range("Cheap", null, 99.99))
                .addRange("Average", 100.0, 499.99)
                .addRange(new RangeAggregator.Range("Expensive", 500.0, null));
        // Facets: count aggregation by skus color
        /*ValueCountAggregationBuilder colorBuilder = AggregationBuilders
                .count(SKUS_COLOR_FIELD);
        // Facets: count aggregation by skus color
        ValueCountAggregationBuilder sizeBuilder = AggregationBuilders
                .count(SKUS_SIZE_FIELD);

        result.add(brandBuilder);*/
        result.add(priceRangeBuilder);
        /*result.add(colorBuilder);
        result.add(sizeBuilder);*/

        return result;
    }

    private ProductResponse getServiceResponse(SearchResponse searchResponse) {
        ProductResponse response = new ProductResponse();

        // Total hits
        response.setTotalHits(searchResponse.getHits().getTotalHits().value);

        // Documents
        List<Map<String, Object>> products = Arrays.stream(searchResponse.getHits().getHits())
                .map(SearchHit::getSourceAsMap)
                .collect(Collectors.toList());
        response.setProducts(products);

        // Facets ():
        Map<String, Map<String, Number>> pricesAgg = new LinkedHashMap<>();

        ParsedRange parsedRange = searchResponse.getAggregations().get(PRICE_AGG);
        parsedRange.getBuckets().stream()
                .sorted(Comparator.comparingDouble(bucket -> (Double) bucket.getFrom()))
                .forEach(bucket -> {
                    String key = bucket.getKeyAsString();
                    Long docCount = bucket.getDocCount();
                    Map<String, Number> bucketValues = new LinkedHashMap<>();
                    bucketValues.put("count", docCount);

                    pricesAgg.put(key, bucketValues);
                });

        response.getFacets().put("Prices", pricesAgg);

        return response;
    }

    private QueryBuilder getQueryByText(String textQuery) {
        List<String> words = Arrays.asList(textQuery.split(" "));
        List<QueryBuilder> mainQueryList = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);

            int maxLevenshteinDistance = getDistanceByTermLength(word);
            List<QueryBuilder> wordQueries = new ArrayList<>();
            // Queries for all possible Levenshtein distances
            for (int distance = 0; distance <= maxLevenshteinDistance; distance++) {
                float boost = getBoostByDistance(distance);
                if (distance == 0) {
                    wordQueries.add(QueryBuilders.matchQuery(NAME_FIELD, word).boost(boost));
                    wordQueries.add(QueryBuilders.matchQuery(BRAND_FIELD, word).boost(boost));
                    wordQueries.add(QueryBuilders.matchQuery(SKUS_COLOR_FIELD, word).boost(boost));
                    wordQueries.add(QueryBuilders.matchQuery(SKUS_SIZE_FIELD, word).boost(boost));
                } else {
                    wordQueries.add(QueryBuilders.matchQuery(NAME_FIELD, word).boost(boost).fuzziness(String.valueOf(distance)));
                    wordQueries.add(QueryBuilders.matchQuery(BRAND_FIELD, word).boost(boost).fuzziness(String.valueOf(distance)));
                    wordQueries.add(QueryBuilders.matchQuery(SKUS_COLOR_FIELD, word).boost(boost).fuzziness(String.valueOf(distance)));
                    wordQueries.add(QueryBuilders.matchQuery(SKUS_SIZE_FIELD, word).boost(boost).fuzziness(String.valueOf(distance)));
                }
            }

            // Prefix query for the last word
            if (i == words.size() - 1) {
                wordQueries.add(QueryBuilders.prefixQuery(NAME_FIELD, word.toLowerCase()).boost(prefixQueryBoost));
                wordQueries.add(QueryBuilders.prefixQuery(BRAND_FIELD, word.toLowerCase()).boost(prefixQueryBoost));
            }

            // Add all queries for the current word to mainQueryList
            if (wordQueries.size() == 1) {
                mainQueryList.add(wordQueries.get(0));
            } else {
                DisMaxQueryBuilder dmqb = QueryBuilders.disMaxQuery().tieBreaker(1.0f);
                wordQueries.forEach(dmqb::add);
                mainQueryList.add(dmqb);
            }
        }

        // Create result query from mainQueryList
        BoolQueryBuilder result = QueryBuilders.boolQuery();
        mainQueryList.forEach(result::must);
        return result;
    }

    private int getDistanceByTermLength(final String token) {
        return token.length() >= fuzzyTwoStartsFromLength
                ? 2
                : (token.length() >= fuzzyOneStartsFromLength ? 1 : 0);
    }

    private float getBoostByDistance(final int distance) {
        return distance == 0
                ? fuzzyZeroBoost
                : (distance == 1 ? fuzzyOneBoost : fuzzyTwoBoost);
    }

    @Override
    public void recreateIndex() {
        productIndexerService.recreateIndex();
    }
}
