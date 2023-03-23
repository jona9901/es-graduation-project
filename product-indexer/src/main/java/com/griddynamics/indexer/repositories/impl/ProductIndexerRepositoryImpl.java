package com.griddynamics.indexer.repositories.impl;

import com.griddynamics.indexer.repositories.ProductIndexerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductIndexerRepositoryImpl implements ProductIndexerRepository {
    private final RestHighLevelClient client;


    @Override
    public void tt() {
        log.info(client.toString());
    }
}
