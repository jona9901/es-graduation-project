package com.griddynamics.indexer.common;

import com.griddynamics.indexer.repositories.ProductIndexerRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest extends BaseTest{
    private final ProductIndexerRepository productIndexerRepository;
    private final APIClient client = new APIClient();

    @Test
    public void recreateIndexTest() {
        productIndexerRepository.recreateIndex();
    }
}
