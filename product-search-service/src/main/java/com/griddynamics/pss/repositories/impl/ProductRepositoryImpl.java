package com.griddynamics.pss.repositories.impl;

import com.griddynamics.pss.models.Aggregation;
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
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.range.ParsedRange;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregator;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
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
    private static final String BRAND_AGG = "brandAgg";
    private static final String COLOR_AGG = "colorAgg";
    private static final String SIZE_AGG = "sizeAgg";
    private static final String NAME_FIELD = "name";
    private static final String NAME_SHINGLES_FIELD = "name.shingles";
    private static final String BRAND_FIELD = "brand";
    private static final String BRAND_SHINGLES_FIELD = "brand.shingles";
    private static final String BRAND_FACETS_FIELD = "brand.facets";
    private static final String PRICE_FIELD = "price";
    private static final String SKUS_COLOR_FIELD = "skus.color";
    private static final String SKUS_SIZE_FIELD = "skus.size";
    private static final String ID_FIELD = "id";
    private static final List<String> SIZES = List.of("xxs", "xs", "s", "m", "l", "xl", "xxl", "xxxl");
    private static final List<String> COLORS = List.of("green", "black", "white", "blue", "yellow", "red", "brown", "orange", "grey");
    private static final String BRAND_FACET_NAME = "brand";
    private static final String PRICE_FACET_NAME = "price";
    private static final String COLOR_FACET_NAME = "color";
    private static final String SIZE_FACET_NAME = "size";
    private static final List<String> PRICE_RANGES = List.of("Cheap", "Average", "Expensive");
    private final RestHighLevelClient client;

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
    @Value("${com.griddynamics.product.search.request.fuzziness.boost.sku.size}")
    int skuSizeBoost;
    @Value("${com.griddynamics.product.search.request.fuzziness.boost.sku.size}")
    int skuColorBoost;
    @Value("${com.griddynamics.product.search.request.fuzziness.boost.shingles}")
    int shinglesBoost;

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
                .from(request.getPage() * request.getSize())
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
        TermsAggregationBuilder brandBuilder = AggregationBuilders
                .terms(BRAND_AGG)
                .field(BRAND_FACETS_FIELD)
                .order(BucketOrder.key(true))
                .order(BucketOrder.count(false));

        // Facets: range aggregation by price
        RangeAggregationBuilder priceRangeBuilder = AggregationBuilders
                .range(PRICE_AGG)
                .field(PRICE_FIELD)
                .keyed(true)
                .addRange(new RangeAggregator.Range(PRICE_RANGES.get(0), null, 100.0)) // Cheap
                .addRange(PRICE_RANGES.get(1), 100.0, 500.0) // Average
                .addRange(new RangeAggregator.Range(PRICE_RANGES.get(2), 500.0, null)); // Expensive

        // Facets: count aggregation by skus color
        TermsAggregationBuilder colorBuilder = AggregationBuilders
                .terms(COLOR_AGG)
                .field(SKUS_COLOR_FIELD)
                .order(BucketOrder.key(true))
                .order(BucketOrder.count(false));

        // Facets: count aggregation by skus size
        TermsAggregationBuilder sizeBuilder = AggregationBuilders
                .terms(SIZE_AGG)
                .field(SKUS_SIZE_FIELD)
                .order(BucketOrder.key(true))
                .order(BucketOrder.count(false));

        result.add(brandBuilder);
        result.add(priceRangeBuilder);
        result.add(colorBuilder);
        result.add(sizeBuilder);

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


        // Facets
        List<Aggregation> pricesAgg = new ArrayList<Aggregation>();
        List<Aggregation> brandAgg = new ArrayList<Aggregation>();
        List<Aggregation> colorAgg = new ArrayList<Aggregation>();
        List<Aggregation> sizesAgg = new ArrayList<Aggregation>();

        // Price range facet
        ParsedRange parsedRange = searchResponse.getAggregations().get(PRICE_AGG);
        parsedRange.getBuckets().stream()
                .sorted(Comparator.comparingDouble(bucket -> (Double) bucket.getFrom()))
                .forEach(bucket -> {
                    String key = bucket.getKeyAsString();
                    Long docCount = bucket.getDocCount();

                    Aggregation bucketValues = new Aggregation(key, docCount);

                    pricesAgg.add(bucketValues);
                });

        // Brand facet
        ParsedTerms brandPrsedTerms = searchResponse.getAggregations().get(BRAND_AGG);
        brandPrsedTerms.getBuckets().stream()
                .forEach(bucket -> {
                    String key = bucket.getKeyAsString();
                    Long docCount = bucket.getDocCount();

                    Aggregation bucketValues = new Aggregation(key, docCount);

                    brandAgg.add(bucketValues);
                });

        // Color facet
        ParsedTerms colorParsedTerms = searchResponse.getAggregations().get(COLOR_AGG);
        colorParsedTerms.getBuckets().stream()
                .forEach(bucket -> {
                    String key = bucket.getKeyAsString();
                    Long docCount = bucket.getDocCount();

                    Aggregation bucketValues = new Aggregation(key, docCount);

                    colorAgg.add(bucketValues);
                });

        // Size facet
        ParsedTerms sizeParsedTerms = searchResponse.getAggregations().get(SIZE_AGG);
        sizeParsedTerms.getBuckets().stream()
                .forEach(bucket -> {
                    String key = bucket.getKeyAsString();
                    Long docCount = bucket.getDocCount();

                    Aggregation bucketValues = new Aggregation(key, docCount);
                    sizesAgg.add(bucketValues);
                });

        response.getFacets().put(PRICE_FACET_NAME, pricesAgg);
        response.getFacets().put(BRAND_FACET_NAME, brandAgg);
        response.getFacets().put(COLOR_FACET_NAME, colorAgg);
        response.getFacets().put(SIZE_FACET_NAME, sizesAgg);

        return response;
    }

    private QueryBuilder getQueryByText(String textQuery) {
        List<String> words = Arrays.asList(textQuery.split(" "));
        List<QueryBuilder> mainQueryList = new ArrayList<>();
        List<QueryBuilder> shingleQueryList = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);

            int maxLevenshteinDistance = getDistanceByTermLength(word);
            List<QueryBuilder> wordQueries = new ArrayList<>();
            // Queries for all possible Levenshtein distances
            for (int distance = 0; distance <= maxLevenshteinDistance; distance++) {
                String size = SIZES.stream()
                        .filter(word.toLowerCase()::equals)
                        .findFirst()
                        .orElse(null);

                String color = COLORS.stream()
                        .filter(word.toLowerCase()::equals)
                        .findFirst()
                        .orElse(null);

                float boost = getBoostByDistance(distance);
                if (distance == 0) {
                    if (size != null) {
                        wordQueries.add(QueryBuilders.matchQuery(SKUS_SIZE_FIELD, word).boost(boost * skuSizeBoost));
                    } else if (color != null) {
                        wordQueries.add(QueryBuilders.matchQuery(SKUS_COLOR_FIELD, word).boost(boost * skuColorBoost));
                    } else {
                        wordQueries.add(QueryBuilders.matchQuery(NAME_FIELD, word).boost(boost));
                        wordQueries.add(QueryBuilders.matchQuery(BRAND_FIELD, word).boost(boost));

                        // Shingles
                        shingleQueryList.add(QueryBuilders.matchQuery(BRAND_SHINGLES_FIELD, textQuery).boost(boost * shinglesBoost));
                        shingleQueryList.add(QueryBuilders.matchQuery(NAME_SHINGLES_FIELD, textQuery).boost(boost * shinglesBoost));
                    }

                } else {
                    if (size != null) {
                        wordQueries.add(QueryBuilders.matchQuery(SKUS_SIZE_FIELD, word).boost(boost * skuSizeBoost).fuzziness(String.valueOf(distance)));
                    } else if (color != null) {
                        wordQueries.add(QueryBuilders.matchQuery(SKUS_COLOR_FIELD, word).boost(boost * skuColorBoost).fuzziness(String.valueOf(distance)));
                    } else {
                        wordQueries.add(QueryBuilders.matchQuery(NAME_FIELD, word).boost(boost).fuzziness(String.valueOf(distance)));
                        wordQueries.add(QueryBuilders.matchQuery(BRAND_FIELD, word).boost(boost).fuzziness(String.valueOf(distance)));

                        shingleQueryList.add(QueryBuilders.matchQuery(BRAND_SHINGLES_FIELD, textQuery).boost(boost * shinglesBoost).fuzziness(String.valueOf(distance)));
                        shingleQueryList.add(QueryBuilders.matchQuery(NAME_SHINGLES_FIELD, textQuery).boost(boost * shinglesBoost).fuzziness(String.valueOf(distance)));
                    }
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
        shingleQueryList.forEach(result::should);

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
}


/*
* @GetMapping("/sku/{sku})
* @PathVariable String sku
* */