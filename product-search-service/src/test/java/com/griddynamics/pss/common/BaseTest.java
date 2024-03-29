package com.griddynamics.pss.common;

import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, MockitoTestExecutionListener.class })
public abstract class BaseTest {
    @LocalServerPort
    private int port;

    public int getSpringBootPort() {
        return port;
    }

    protected class APIClient {

        private boolean logResponse = false;
        private boolean isIndexerTest = false;
        private String productEndpoint = "/v1/product";
        private String indexerEndpoint = "/v1/recreate";
        private int indexerPort = 8081;

        public APIClient(boolean isIndexerTest) {
            this.isIndexerTest = isIndexerTest;
        }

        /**
         * Use this method to log the response to debug tests
         */
        public APIClient logResponse() {
            this.logResponse = true;
            return this;
        }

        public RequestSpecification productRequest() {
            if(!isIndexerTest) {
                return baseRequest()
                        .basePath(productEndpoint)
                        .header("Content-Type", "application/json");
            } else {
                return baseRequest()
                        .basePath(indexerEndpoint)
                        .header("Content-Type", "application/json");
            }
        }

        public RequestSpecification baseRequest() {
            RequestSpecification requestSpecification;
            if(!isIndexerTest) {
                requestSpecification = given()
                        .baseUri("http://localhost").port(getSpringBootPort())
                        .log().all();
            } else {
                requestSpecification = given()
                        .baseUri("http://localhost").port(indexerPort)
                        .log().all();
            }

            if (logResponse) {
                requestSpecification = requestSpecification.filter(new ResponseLoggingFilter());
            }

            return requestSpecification;
        }
    }
}

