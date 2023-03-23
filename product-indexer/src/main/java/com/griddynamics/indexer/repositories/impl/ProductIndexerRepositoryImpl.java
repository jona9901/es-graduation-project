package com.griddynamics.indexer.repositories.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.griddynamics.indexer.models.Product;
import com.griddynamics.indexer.models.mappers.ProductToXcontent;
import com.griddynamics.indexer.repositories.ProductIndexerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.griddynamics.indexer.exceptions.advisers.ConsumerHandler.consumerHandlerBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductIndexerRepositoryImpl implements ProductIndexerRepository {
    private final RestHighLevelClient client;
    private final ProductToXcontent productToXcontent;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAXIMUM_NUMBER_OF_INDICES = 3;
    @Value("${com.griddynamics.product.indexer.es.index}")
    private String indexName;
    @Value("${com.griddynamics.product.indexer.date.format}")
    private String dateFormat;
    @Value("${com.griddynamics.product.indexer.files.mappings}")
    private Resource productIndexerMappingsFile;
    @Value("${com.griddynamics.product.indexer.files.settings}")
    private Resource productIndexerSettingsFile;
    @Value("${com.griddynamics.product.indexer.files.data}")
    private Resource productIndexerDataFile;

    @Override
    public void recreateIndex() {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        String formattedDate = date.format(formatter);

        String indexNameDate = String.format("%s_%s", indexName, formattedDate);

        if (indexExists(indexNameDate)) {
            deleteIndex(indexNameDate);
        }

        String settings = getStrFromResource(productIndexerSettingsFile);
        String mappings = getStrFromResource(productIndexerMappingsFile);
        createIndex(indexNameDate, settings, mappings);

        GetIndexRequest indexRequest = new GetIndexRequest(String.format("%s_*", indexName));

        // Alias
        IndicesAliasesRequest request = new IndicesAliasesRequest();

        List.of(indexRequest.indices()).stream()
                .map(index -> {
                    return new AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
                            .index(index)
                            .alias(indexName);
                })
                .forEach(request::addAliasAction);

        IndicesAliasesRequest.AliasActions aliasAction =
                new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                        .index(indexNameDate)
                        .alias(indexName);
        request.addAliasAction(aliasAction);

        try {
            client.indices().updateAliases(request, RequestOptions.DEFAULT);

            GetIndexResponse indexResponse = client.indices().get(indexRequest, RequestOptions.DEFAULT);
            if (indexResponse.getIndices().length > MAXIMUM_NUMBER_OF_INDICES) {
                deleteIndex(indexResponse.getIndices()[0]);
            }

        } catch (IOException ioException) {
            log.error(ioException.getMessage());
        }
        processBulkInsertData();
    }

    private void createIndex(String indexName, String settings, String mappings) {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName)
                .mapping(mappings, XContentType.JSON)
                .settings(settings, XContentType.JSON);

        CreateIndexResponse createIndexResponse;
        try {
            createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        } catch (IOException ex) {
            throw new RuntimeException("An error occurred during creating ES index.", ex);
        }

        if (!createIndexResponse.isAcknowledged()) {
            throw new RuntimeException("Creating index not acknowledged for indexName: " + indexName);
        } else {
            log.info("Index {} has been created.", indexName);
        }
    }

    private boolean indexExists(String indexName) {
        GetIndexRequest existsRequest = new GetIndexRequest(indexName);

        try {
            return client.indices().exists(existsRequest, RequestOptions.DEFAULT);
        } catch (IOException ex) {
            throw new RuntimeException("Existence checking is failed for index " + indexName, ex);
        }
    }

    private void deleteIndex(String indexName) {
        try {
            DeleteIndexRequest deleteRequest = new DeleteIndexRequest(indexName);
            AcknowledgedResponse acknowledgedResponse = client.indices().delete(deleteRequest, RequestOptions.DEFAULT);
            if (!acknowledgedResponse.isAcknowledged()) {
                log.warn("Index deletion is not acknowledged for indexName: {}", indexName);
            } else {
                log.info("Index {} has been deleted.", indexName);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Deleting of old index version is failed for indexName: " + indexName, ex);
        }
    }

    private static String getStrFromResource(Resource resource) {
        try {
            if (!resource.exists()) {
                throw new IllegalArgumentException("File not found: " + resource.getFilename());
            }
            return Resources.toString(resource.getURL(), Charsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Can not read resource file: " + resource.getFilename(), ex);
        }
    }

    // TODO: refractor stream
    private void processBulkInsertData() {
        int requestCnt = 0;
        try {
            BulkRequest bulkRequest = new BulkRequest();
            List<Product> products = objectMapper.readValue(productIndexerDataFile.getFile(), new TypeReference<List<Product>>() {});
            List<XContentBuilder> builder = products.stream()
                    .peek(consumerHandlerBuilder(product -> {
                        AnalyzeRequest analyzeRequest = new AnalyzeRequest(indexName)
                                .analyzer("shingle_analyzer")
                                .field("name")
                                .text(product.getName());
                        AnalyzeResponse response = client
                                .indices()
                                .analyze(analyzeRequest, RequestOptions.DEFAULT);
                        List<String> shingles = response
                                .getTokens()
                                .stream()
                                .map(AnalyzeResponse.AnalyzeToken::getTerm)
                                .collect(Collectors.toList());
                        product.setNameShingles(shingles);
                    }))
                    .map(productToXcontent::productToXcontentBuilder)
                    .collect(Collectors.toList());

            List<IndexRequest> requests = builder.stream()
                    .map(build -> {
                        return new IndexRequest(indexName)
                                .source(build);
                    })
                    .collect(Collectors.toList());

            for (int i = 0; i < products.size(); i++) {
                requests.get(i).id(Integer.toString(products.get(i).getId()));
                bulkRequest.add(requests.get(i));
                requestCnt++;
            }

            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

            if (bulkResponse.getItems().length != requestCnt) {
                log.warn("Only {} out of {} requests have been processed in a bulk request.", bulkResponse.getItems().length, requestCnt);
            } else {
                log.info("{} requests have been processed in a bulk request.", bulkResponse.getItems().length);
            }

            if (bulkResponse.hasFailures()) {
                log.warn("Bulk data processing has failures:\n{}", bulkResponse.buildFailureMessage());
            }
        } catch (IOException ioException) {
            log.error(ioException.getMessage());
        }
    }
}
