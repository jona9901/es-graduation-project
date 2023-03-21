package com.griddynamics.esgraduationproject.common;

import io.restassured.RestAssured;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static io.restassured.RestAssured.given;

//@TestPropertySource(locations = { "classpath:integration-test.properties" })
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

        /**
         * Use this method to log the response to debug tests
         */
        public APIClient logResponse() {
            this.logResponse = true;
            return this;
        }

        public RequestSpecification typeaheadRequest() {
            return baseRequest()
                .basePath("/v1/typeahead")
                .header("Content-Type", "application/json");
        }

        public RequestSpecification baseRequest() {
            RequestSpecification requestSpecification = given()
                .baseUri("http://localhost").port(getSpringBootPort())
                .log().all();

            if (logResponse) {
                requestSpecification = requestSpecification.filter(new ResponseLoggingFilter());
            }

            return requestSpecification;
        }

    }
}
