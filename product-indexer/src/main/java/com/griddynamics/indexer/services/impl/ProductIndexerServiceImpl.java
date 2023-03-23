package com.griddynamics.indexer.services.impl;

import com.griddynamics.indexer.repositories.ProductIndexerRepository;
import com.griddynamics.indexer.services.ProductIndexerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductIndexerServiceImpl implements ProductIndexerService {
    private final ProductIndexerRepository repository;


    @Override
    public void tt() {
        repository.tt();
    }
}
