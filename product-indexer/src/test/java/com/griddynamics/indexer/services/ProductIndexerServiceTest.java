package com.griddynamics.indexer.services;

import com.griddynamics.indexer.common.TestConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@SpringBootTest
@ContextConfiguration(classes = {TestConfig.class})
public class ProductIndexerServiceTest {
    @Autowired
    ProductIndexerService productIndexerService;
    @Test
    public void demoTest() {
        productIndexerService.tt();
    }

    @SpringBootApplication
    static class TestConfiguration {
    }
}
