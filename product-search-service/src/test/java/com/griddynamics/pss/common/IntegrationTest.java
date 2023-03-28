package com.griddynamics.pss.common;

import com.griddynamics.pss.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest extends  BaseTest{
    private final APIClient client = new APIClient();

    private final ProductService productService;

    @BeforeAll
    public void init() throws InterruptedException {
        productService.recreateIndex();
        Thread.sleep(1100); // TASK 6: Why if we change 1100 to 500, then some tests fail? How to fix it, so that all tests pass with 500?
    }

    // Empty request test
    @Test
    public void emptyRequestTest() {
        client
                .productRequest()
                .body("{}")
                .post()
                .then()
                .statusCode(400);
                //.body("totalHits", equalTo(0));
    }

    @Test
    public void wrongWordTest() {
        client
                .productRequest()
                .body("{\"queryText\": \"Calvin klein L blue ankle skinny jeans wrongword\"}")
                .post()
                .then()
                .statusCode(200)
                .body("totalHits", equalTo(0));
    }

    @Test
    public void notExistingDocTest() {
        client
                .productRequest()
                .body("{\"queryText\": \"Calvin klein L red ankle skinny jeans\"}")
                .post()
                .then()
                .statusCode(200)
                .body("totalHits", equalTo(0));
    }

    // Happy path test
    @Test
    public void happyPathTest() {
        client
                .logResponse() // Use this method to log the response to debug tests
                .productRequest()
                .body("{\"queryText\": \"Calvin klein L blue ankle skinny jeans\"}")
                .post()
                .then()
                .statusCode(200)
                // TotalHits
                .body("totalHits", is(1))
                // Typeaheads
                .body("products", hasSize(1))
                .body("products[0].id", is(2))
                .body("products[0].brand", is("Calvin Klein"))
                .body("products[0].name", is("Women ankle skinny jeans, model 1282"))
                .body("products[0].skus", hasSize(9));
    }

    // Pagination Test
    @Test
    public void paginationTest() {
        client
                .productRequest()
                .body("{" +
                            "\"queryText\": \"jeans\",\n" +
                            "\"size\": \"2\",\n" +
                            "\"page\": \"1\"\n" +
                        "}")
                .post()
                .then()
                .statusCode(200)
                .body("totalHits", is(8))
                .body("products", hasSize(2));
                /*.body("products[0].id", is(6))
                .body("products[1].id", is(5));*/
    }
}
