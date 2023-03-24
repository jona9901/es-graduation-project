package com.griddynamics.indexer.common;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

@Configuration
@ComponentScan(basePackages = {"com.griddynamics"})
@TestPropertySource(locations = { "classpath:product-indexer-test.yml" })
public class TestConfig {
}
