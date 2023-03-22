package com.griddynamics.indexer.services;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ProductIndexerServiceTest {
    @Autowired
    ProductIndexerService productIndexerService;
    @Test
    public void jejeTTest() {
        productIndexerService.jejeT();
    }

    @SpringBootApplication
    static class TestConfiguration {
    }
}
