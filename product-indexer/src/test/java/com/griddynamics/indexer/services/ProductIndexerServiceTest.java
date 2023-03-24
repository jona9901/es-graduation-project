package com.griddynamics.indexer.services;

import com.griddynamics.indexer.common.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {TestConfig.class})
public class ProductIndexerServiceTest {
    @Autowired
    ProductIndexerService productIndexerService;

    @Test
    public void demoTest() {
        productIndexerService.recreateIndex();
    }

    @SpringBootApplication
    static class TestConfiguration {
    }
}
