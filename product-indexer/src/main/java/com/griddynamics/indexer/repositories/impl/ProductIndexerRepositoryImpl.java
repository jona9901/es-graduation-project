package com.griddynamics.indexer.repositories.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.griddynamics.indexer.models.Product;
import com.griddynamics.indexer.repositories.ProductIndexerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
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
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductIndexerRepositoryImpl implements ProductIndexerRepository {
    private final RestHighLevelClient client;

    private static final ObjectMapper objectMapper = new ObjectMapper();

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
        processBulkInsertData();
        /*LocalDateTime date = LocalDateTime.now();
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
                .forEach(index -> {
                    IndicesAliasesRequest.AliasActions removeActions = new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
                            .index(index)
                            .alias(indexName);
                    request.addAliasAction(removeActions);
                });

        IndicesAliasesRequest.AliasActions aliasAction =
                new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                        .index(indexNameDate)
                        .alias(indexName);
        request.addAliasAction(aliasAction);

        try {
            client.indices().updateAliases(request, RequestOptions.DEFAULT);

            GetIndexResponse indexResponse = client.indices().get(indexRequest, RequestOptions.DEFAULT);
            if (indexResponse.getIndices().length > 5) {
                deleteIndex(indexResponse.getIndices()[0]);
            }

        } catch (IOException ioException) {
            log.error(ioException.getMessage());
        }*/
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

    private void processBulkInsertData() {
        int requestCnt = 0;
        try {
            BulkRequest bulkRequest = new BulkRequest();
            List<Product> products = objectMapper.readValue(productIndexerDataFile.getFile(), new TypeReference<List<Product>>() {});
            /*products.stream()
                    .peek(product -> {

                    });*/
        } catch (IOException ioException) {
            log.error(ioException.getMessage());
        }
    }

    /*private void processBulkInsertData(Resource bulkInsertDataFile) {
        int requestCnt = 0;
        try {
            BulkRequest bulkRequest = new BulkRequest();
            BufferedReader br = new BufferedReader(new InputStreamReader(bulkInsertDataFile.getInputStream()));

            while (br.ready()) {
                String line1 = br.readLine(); // action_and_metadata
                if (isNotEmpty(line1) && br.ready()) {
                    requestCnt++;
                    String line2 = br.readLine();
                    IndexRequest indexRequest = createIndexRequestFromBulkData(line1, line2);
                    if (indexRequest != null) {
                        bulkRequest.add(indexRequest);
                    }
                }
            }

            BulkResponse bulkResponse = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (bulkResponse.getItems().length != requestCnt) {
                log.warn("Only {} out of {} requests have been processed in a bulk request.", bulkResponse.getItems().length, requestCnt);
            } else {
                log.info("{} requests have been processed in a bulk request.", bulkResponse.getItems().length);
            }

            if (bulkResponse.hasFailures()) {
                log.warn("Bulk data processing has failures:\n{}", bulkResponse.buildFailureMessage());
            }
        } catch (IOException ex) {
            log.error("An exception occurred during bulk data processing", ex);
            throw new RuntimeException(ex);
        }
    }*/
}
